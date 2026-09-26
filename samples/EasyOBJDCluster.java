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
import org.firstinspires.ftc.easyobjd.OverlayMode;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.List;

/**
 * Copy this file and {@code EasyOBJDUserConfig} into TeamCode. Preview is the
 * 12×12 cluster grid (FULL overlay). Touching yellow cells become one cluster;
 * telemetry is occupied cells and each cluster's camera X/Y (inches).
 *
 * <p>Camera height, tilt, and focal length come from {@code EasyOBJDUserConfig}.
 * Inches are only right after you run <b>EasyOBJD Range Test</b> on your robot
 * and paste its numbers there.</p>
 *
 * <p>Gamepad 1: D-pad up/down HSV, left/right MASK / GRID / FULL.</p>
 *
 * <p>TeamCode will not resolve {@code org.firstinspires.ftc.easyobjd} until
 * JitPack is a repository and TeamCode depends on the library:</p>
 * <pre>
 * maven { url = 'https://jitpack.io' }
 * implementation 'org.openftc:easyopencv:1.7.3'
 * implementation 'com.github.IamAki123:EasyOBJD:1.0.3'
 * </pre>
 * Then File → Sync Project with Gradle Files.
 */
//noinspection SpellCheckingInspection
@SuppressWarnings("unused")
@TeleOp(name = "EasyOBJD Cluster", group = "EasyOBJD")
public class EasyOBJDCluster extends OpMode {
    public static final String WEBCAM_NAME = "Webcam 1";

    private static final OverlayMode[] OVERLAYS = {
            OverlayMode.MASK, OverlayMode.GRID, OverlayMode.FULL
    };

    private OpenCvWebcam webcam;
    private EasyOBJDPipeline pipeline;
    private volatile boolean cameraInitialized = false;

    @SuppressLint("DiscouragedApi")
    @Override
    public void init() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources()
                .getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(
                hardwareMap.get(WebcamName.class, WEBCAM_NAME), cameraMonitorViewId);
        pipeline = EasyOBJD.createPipeline();
        EasyOBJDConfig cfg = pipeline.getConfig();
        cfg.cameraHeightInches = EasyOBJDUserConfig.CAMERA_HEIGHT_INCHES;
        cfg.cameraTiltDegrees = EasyOBJDUserConfig.CAMERA_TILT_DEGREES;
        cfg.horizontalFovDegrees = EasyOBJDUserConfig.HORIZONTAL_FOV_DEGREES;
        cfg.focalLengthPixelsAt640 = EasyOBJDUserConfig.FOCAL_LENGTH_PIXELS_AT_640;
        cfg.ballDiameterInches = EasyOBJDUserConfig.BALL_DIAMETER_INCHES;
        cfg.overlayMode = OverlayMode.FULL;
        webcam.setPipeline(pipeline);

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT,
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
        if (gamepad1.dpadUpWasPressed()) {
            pipeline.adjustHsvRange(1);
        }
        if (gamepad1.dpadDownWasPressed()) {
            pipeline.adjustHsvRange(-1);
        }
        if (gamepad1.dpadRightWasPressed()) {
            cycleOverlay(1);
        }
        if (gamepad1.dpadLeftWasPressed()) {
            cycleOverlay(-1);
        }

        if (!cameraInitialized) {
            telemetry.addLine("Camera starting...");
        }

        List<ClusterInfo> clusters = pipeline.getClusters();
        telemetry.addLine(String.format("CLUSTERS: %d    BALLS: %d",
                clusters.size(), pipeline.getBallCount()));
        if (clusters.isEmpty()) {
            telemetry.addLine("  none - press D-pad UP to widen HSV");
        }
        for (ClusterInfo cluster : clusters) {
            double fromFront = cluster.y - EasyOBJDUserConfig.CAMERA_BEHIND_FRONT_INCHES
                    - EasyOBJDUserConfig.BALL_DIAMETER_INCHES / 2.0;
            telemetry.addLine(String.format("  #%d   %.1f in ahead   %.1f in %s",
                    cluster.id, fromFront, Math.abs(cluster.x), cluster.x >= 0 ? "right" : "left"));
        }

        EasyOBJDConfig cfg = pipeline.getConfig();
        telemetry.addLine("");
        telemetry.addLine(String.format("Overlay: %s", cfg.overlayMode));
        telemetry.addLine(String.format("HSV low:  %.0f, %.0f, %.0f",
                cfg.hsvLower.val[0], cfg.hsvLower.val[1], cfg.hsvLower.val[2]));
        telemetry.addLine(String.format("HSV high: %.0f, %.0f, %.0f",
                cfg.hsvUpper.val[0], cfg.hsvUpper.val[1], cfg.hsvUpper.val[2]));
        telemetry.addLine("");
        telemetry.addLine("Ahead = robot front to near edge of ball");
        telemetry.addLine("D-pad up/down: HSV   left/right: overlay");
        telemetry.update();
    }

    private void cycleOverlay(int step) {
        OverlayMode current = pipeline.getConfig().overlayMode;
        int idx = 0;
        for (int i = 0; i < OVERLAYS.length; i++) {
            if (OVERLAYS[i] == current) {
                idx = i;
                break;
            }
        }
        pipeline.getConfig().overlayMode = OVERLAYS[Math.floorMod(idx + step, OVERLAYS.length)];
    }

    @Override
    public void stop() {
        if (webcam != null) {
            webcam.stopStreaming();
            webcam.closeCameraDevice();
        }
    }
}
