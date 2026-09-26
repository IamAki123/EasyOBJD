/*
 * Copyright (c) 2026 Akash Vijay Aradhya
 *
 * SPDX-License-Identifier: MIT
 *
 * EasyOBJD - FTC EasyOpenCV object detection
 * https://github.com/IamAki123/EasyOBJD
 */
package org.firstinspires.ftc.teamcode;

import android.annotation.SuppressLint;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.easyobjd.ClusterInfo;
import org.firstinspires.ftc.easyobjd.EasyOBJD;
import org.firstinspires.ftc.easyobjd.EasyOBJDConfig;
import org.firstinspires.ftc.easyobjd.EasyOBJDPipeline;
import org.firstinspires.ftc.easyobjd.LocalizationMath;
import org.firstinspires.ftc.easyobjd.OverlayMode;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.ArrayList;
import java.util.List;

/**
 * Tape-measure range check and camera-mount calibration.
 *
 * <p>Put one ball straight ahead of the camera. Tape from the front of the
 * robot to the near edge of the ball, along the floor. Set that as the target
 * with the D-pad <b>before</b> pressing A, and
 * the OpMode solves the camera tilt that makes that ball read correctly. A
 * second point at a different distance (8+ in apart) also solves focal
 * length, which fixes a wrong FOV. Copy the telemetry values into
 * {@code EasyOBJDUserConfig}. Copy that file into TeamCode too; the starting
 * mount is read from it.</p>
 *
 * <p>Gamepad 1: D-pad up/down target ±1 in, left/right ±6 in; A capture;
 * B reset; bumpers HSV tighter/wider; X overlay.</p>
 *
 * <p>Calibration math is inline so this runs on JitPack 1.0.2 and later.</p>
 */
@SuppressWarnings("unused")
@TeleOp(name = "EasyOBJD Range Test", group = "EasyOBJD")
public class EasyOBJDRangeTest extends OpMode {
    public static final String WEBCAM_NAME = EasyOBJDUserConfig.WEBCAM_NAME;
    public static final int STREAM_WIDTH = 640;
    public static final int STREAM_HEIGHT = 480;

    // Starting mount comes from EasyOBJDUserConfig. Measure height and
    // behind-front there first; this OpMode solves tilt and focal.
    private static final double CAMERA_HEIGHT_INCHES = EasyOBJDUserConfig.CAMERA_HEIGHT_INCHES;
    private static final double CAMERA_BEHIND_FRONT_INCHES = EasyOBJDUserConfig.CAMERA_BEHIND_FRONT_INCHES;
    private static final double CAMERA_TILT_DEGREES = EasyOBJDUserConfig.CAMERA_TILT_DEGREES;
    private static final double HORIZONTAL_FOV_DEGREES = EasyOBJDUserConfig.HORIZONTAL_FOV_DEGREES;
    private static final double FOCAL_LENGTH_PIXELS_AT_640 = EasyOBJDUserConfig.FOCAL_LENGTH_PIXELS_AT_640;
    private static final double BALL_DIAMETER_INCHES = EasyOBJDUserConfig.BALL_DIAMETER_INCHES;

    private static final int FRAMES_PER_CAPTURE = 15;
    private static final double MIN_POINT_SPACING_INCHES = 8.0;

    private OpenCvWebcam webcam;
    private EasyOBJDPipeline pipeline;
    private volatile boolean cameraInitialized = false;

    /** Front of robot to near edge of the ball. */
    private double targetInches = 24.0;
    private double tiltDegrees = CAMERA_TILT_DEGREES;
    private double focalAt640 = FOCAL_LENGTH_PIXELS_AT_640;
    private boolean focalSolved = false;

    /** Each entry: {image row of the ball, taped forward inches}. */
    private final List<double[]> points = new ArrayList<double[]>();
    private int captureFramesLeft = 0;
    private double captureSumY = 0;
    private int captureCount = 0;
    private long lastFrame = -1;
    private String status = "";

    @SuppressLint("DiscouragedApi")
    @Override
    public void init() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources()
                .getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(
                hardwareMap.get(WebcamName.class, WEBCAM_NAME), cameraMonitorViewId);
        pipeline = EasyOBJD.createPipeline();
        EasyOBJDConfig cfg = pipeline.getConfig();
        cfg.cameraHeightInches = CAMERA_HEIGHT_INCHES;
        cfg.ballDiameterInches = BALL_DIAMETER_INCHES;
        cfg.horizontalFovDegrees = HORIZONTAL_FOV_DEGREES;
        cfg.overlayMode = OverlayMode.FULL;
        applyMount();
        webcam.setPipeline(pipeline);

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(STREAM_WIDTH, STREAM_HEIGHT, OpenCvCameraRotation.UPRIGHT,
                        OpenCvWebcam.StreamFormat.MJPEG);
                cameraInitialized = true;
            }

            @Override
            public void onError(int errorCode) {
                cameraInitialized = false;
                telemetry.addData("Camera Error", errorCode);
            }
        });
    }

    @Override
    public void init_loop() {
        tick();
    }

    @Override
    public void loop() {
        tick();
    }

    private void tick() {
        handleInput();
        ClusterInfo nearest = nearestCluster(pipeline.getClusters());
        collectCapture(nearest);

        if (!cameraInitialized) {
            telemetry.addLine("Camera starting...");
        }

        telemetry.addLine(nextStep());
        telemetry.addLine("");
        telemetry.addLine(String.format("TARGET:  %.0f in   (tape: robot front to ball edge)",
                targetInches));
        if (nearest == null) {
            telemetry.addLine("SEEING:  no ball - press right bumper to widen HSV");
        } else {
            double nowFront = fromFront(nearest.y);
            telemetry.addLine(String.format("SEEING:  %.1f in   (off by %+.1f)",
                    nowFront, nowFront - targetInches));
            if (!points.isEmpty()) {
                double startFront = fromFront(startMountForward(nearest));
                telemetry.addLine(String.format("  before calibrating: %.1f in", startFront));
            }
        }
        if (!status.isEmpty()) {
            telemetry.addLine(status);
        }

        telemetry.addLine("");
        telemetry.addLine("PASTE INTO EasyOBJDUserConfig:");
        telemetry.addLine(String.format("  CAMERA_TILT_DEGREES = %.2f", tiltDegrees));
        telemetry.addLine(String.format("  FOCAL_LENGTH_PIXELS_AT_640 = %.1f%s",
                focalAt640, focalSolved ? "" : "   (needs 2nd capture)"));

        telemetry.addLine("");
        telemetry.addLine("D-pad: target +-1 / +-6   A: capture   B: start over");
        telemetry.addLine("Bumpers: HSV   X: overlay");
        telemetry.update();
    }

    private String nextStep() {
        if (captureFramesLeft > 0) {
            return "CAPTURING... hold still";
        }
        if (points.isEmpty()) {
            return "STEP 1: ball ~2 ft ahead. Set TARGET to your tape, press A";
        }
        if (!focalSolved) {
            return "STEP 2: move ball ~2 ft farther. Set TARGET, press A";
        }
        return "DONE: paste the numbers below, then try other distances";
    }

    private void handleInput() {
        if (gamepad1.dpadUpWasPressed()) {
            targetInches += 1;
        }
        if (gamepad1.dpadDownWasPressed()) {
            targetInches = Math.max(6, targetInches - 1);
        }
        if (gamepad1.dpadRightWasPressed()) {
            targetInches += 6;
        }
        if (gamepad1.dpadLeftWasPressed()) {
            targetInches = Math.max(6, targetInches - 6);
        }
        if (gamepad1.rightBumperWasPressed()) {
            pipeline.adjustHsvRange(1);
        }
        if (gamepad1.leftBumperWasPressed()) {
            pipeline.adjustHsvRange(-1);
        }
        if (gamepad1.xWasPressed()) {
            pipeline.cycleOverlayMode();
        }
        if (gamepad1.aWasPressed() && captureFramesLeft == 0) {
            captureFramesLeft = FRAMES_PER_CAPTURE;
            captureSumY = 0;
            captureCount = 0;
            status = "";
        }
        if (gamepad1.bWasPressed()) {
            points.clear();
            captureFramesLeft = 0;
            tiltDegrees = CAMERA_TILT_DEGREES;
            focalAt640 = FOCAL_LENGTH_PIXELS_AT_640;
            focalSolved = false;
            applyMount();
            status = "Started over";
        }
    }

    /** Averages the nearest cluster's image row over several new frames. */
    private void collectCapture(ClusterInfo nearest) {
        if (captureFramesLeft == 0) {
            return;
        }
        long frame = pipeline.getFrameCount();
        if (frame == lastFrame) {
            return;
        }
        lastFrame = frame;
        captureFramesLeft--;
        if (nearest != null) {
            captureSumY += nearest.centerPx.y;
            captureCount++;
        }
        if (captureFramesLeft > 0) {
            return;
        }
        if (captureCount < FRAMES_PER_CAPTURE / 2) {
            status = "Capture failed: no ball seen. Try again.";
            return;
        }
        points.add(new double[] { captureSumY / captureCount, lensForward(targetInches) });
        solve();
    }

    private void solve() {
        int w = processWidth();
        int h = processHeight(w);

        double[] a = null;
        double[] b = null;
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                double spacing = Math.abs(points.get(i)[1] - points.get(j)[1]);
                if (spacing >= MIN_POINT_SPACING_INCHES
                        && (a == null || spacing > Math.abs(a[1] - b[1]))) {
                    a = points.get(i);
                    b = points.get(j);
                }
            }
        }
        focalSolved = false;
        if (a != null) {
            double f640 = focalForTwoFloorPoints(a[0], a[1], b[0], b[1], w, h);
            double fov = Double.isNaN(f640) ? Double.NaN
                    : Math.toDegrees(2.0 * Math.atan(320.0 / f640));
            if (fov >= 30 && fov <= 120) {
                focalAt640 = f640;
                focalSolved = true;
            }
        }

        double focalPx = LocalizationMath.focalPx(focalAt640, HORIZONTAL_FOV_DEGREES, w);
        double sum = 0;
        for (double[] p : points) {
            sum += tiltForFloorPoint(p[0], h, focalPx, p[1]);
        }
        tiltDegrees = sum / points.size();
        applyMount();
        status = String.format("Saved capture at %.0f in (%d total)",
                fromFront(points.get(points.size() - 1)[1]), points.size());
        if (points.size() >= 2 && !focalSolved) {
            status += ". Too close to the other capture - move 8+ in farther.";
        }
    }

    private void applyMount() {
        EasyOBJDConfig cfg = pipeline.getConfig();
        cfg.cameraTiltDegrees = tiltDegrees;
        cfg.focalLengthPixelsAt640 = focalAt640;
    }

    private double tiltForFloorPoint(double pixelY, int frameHeight, double focalPx, double forward) {
        double drop = CAMERA_HEIGHT_INCHES - BALL_DIAMETER_INCHES / 2.0;
        double aboveAxis = Math.atan2(frameHeight / 2.0 - pixelY, focalPx);
        return Math.toDegrees(aboveAxis + Math.atan2(drop, forward));
    }

    private double focalForTwoFloorPoints(double py1, double z1, double py2, double z2,
                                          int frameWidth, int frameHeight) {
        double a1 = frameHeight / 2.0 - py1;
        double a2 = frameHeight / 2.0 - py2;
        if (Math.abs(a1 - a2) < 0.05 * frameHeight) {
            return Double.NaN;
        }
        double drop = CAMERA_HEIGHT_INCHES - BALL_DIAMETER_INCHES / 2.0;
        double target = Math.atan2(drop, z2) - Math.atan2(drop, z1);
        double lo = Math.max(0.3 * frameWidth, Math.sqrt(Math.max(0, a1 * a2)) + 1.0);
        double hi = 5.0 * frameWidth;
        double gLo = Math.atan2(a1, lo) - Math.atan2(a2, lo) - target;
        double gHi = Math.atan2(a1, hi) - Math.atan2(a2, hi) - target;
        if (gLo * gHi > 0) {
            return Double.NaN;
        }
        for (int i = 0; i < 60; i++) {
            double mid = 0.5 * (lo + hi);
            double gMid = Math.atan2(a1, mid) - Math.atan2(a2, mid) - target;
            if (gLo * gMid <= 0) {
                hi = mid;
            } else {
                lo = mid;
                gLo = gMid;
            }
        }
        return 0.5 * (lo + hi) * (640.0 / frameWidth);
    }

    /** Robot-front-to-near-edge tape distance → lens-to-ball-center forward inches. */
    private static double lensForward(double fromFrontInches) {
        return fromFrontInches + CAMERA_BEHIND_FRONT_INCHES + BALL_DIAMETER_INCHES / 2.0;
    }

    private static double fromFront(double lensForwardInches) {
        return lensForwardInches - CAMERA_BEHIND_FRONT_INCHES - BALL_DIAMETER_INCHES / 2.0;
    }

    /** Forward inches for this cluster using the untouched starting mount. */
    private double startMountForward(ClusterInfo cluster) {
        int w = processWidth();
        int h = processHeight(w);
        double[] floor = LocalizationMath.pixelToFloor(
                cluster.centerPx.x, cluster.centerPx.y, w, h,
                CAMERA_HEIGHT_INCHES, BALL_DIAMETER_INCHES,
                FOCAL_LENGTH_PIXELS_AT_640, HORIZONTAL_FOV_DEGREES, CAMERA_TILT_DEGREES);
        return floor == null ? Double.NaN : floor[2];
    }

    /** Lowest cluster in the image is the closest one on the floor. */
    private static ClusterInfo nearestCluster(List<ClusterInfo> clusters) {
        ClusterInfo best = null;
        for (ClusterInfo c : clusters) {
            if (best == null || c.centerPx.y > best.centerPx.y) {
                best = c;
            }
        }
        return best;
    }

    private int processWidth() {
        EasyOBJDConfig cfg = pipeline.getConfig();
        if (cfg.processWidth > 0) {
            return cfg.processWidth;
        }
        if (cfg.processScale < 0.999) {
            return (int) Math.round(STREAM_WIDTH * cfg.processScale);
        }
        return STREAM_WIDTH;
    }

    private static int processHeight(int processWidth) {
        return (int) Math.round(STREAM_HEIGHT * processWidth / (double) STREAM_WIDTH);
    }

    @Override
    public void stop() {
        if (webcam != null) {
            webcam.stopStreaming();
            webcam.closeCameraDevice();
        }
    }
}
