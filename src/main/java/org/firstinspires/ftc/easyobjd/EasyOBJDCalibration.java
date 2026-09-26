/*
 * Copyright (c) 2026 Akash Vijay Aradhya
 *
 * SPDX-License-Identifier: MIT
 *
 * EasyOBJD - FTC EasyOpenCV object detection
 * https://github.com/IamAki123/EasyOBJD
 */
package org.firstinspires.ftc.easyobjd;

/**
 * Tape-measure helpers for {@link EasyOBJDConfig#focalLengthPixelsAt640} and
 * {@link EasyOBJDConfig#cameraTiltDegrees}.
 *
 * <p>Place one official-size ball on the floor at a known distance, centered
 * in the image. Read the apparent radius from the sample / debug overlay,
 * then set:
 *
 * <pre>
 * config.focalLengthPixelsAt640 = EasyOBJDCalibration.focalLengthAt640(radiusPx, distanceIn, frameWidth);
 * config.cameraTiltDegrees = EasyOBJDCalibration.suggestedTiltDegrees(cameraHeightIn, distanceIn);
 * </pre>
 *
 * <p>{@link #suggestedTiltDegrees} assumes the ball sits on the optical axis
 * (image center). {@link #tiltForFloorPoint} uses the ball's actual image row
 * and is the one to trust; {@link #focalForTwoFloorPoints} also fixes a wrong
 * FOV from two taped distances. Checkerboard / AprilTag extrinsics can replace
 * these later; these helpers stay tape-only.
 */
public class EasyOBJDCalibration {
    public static final double DEFAULT_BALL_DIAMETER_INCHES = 2.8;
    public static final int CALIBRATION_WIDTH = LocalizationMath.CALIBRATION_WIDTH;

    protected EasyOBJDCalibration() {}

    /**
     * Focal length at 640-wide from a measured apparent radius.
     * {@code FOCAL = (2 * radiusPx * distance) / diameter}, then scaled to 640.
     */
    public static double focalLengthAt640(double apparentRadiusPx, double distanceInches, int frameWidth) {
        return focalLengthAt640(apparentRadiusPx, distanceInches, frameWidth, DEFAULT_BALL_DIAMETER_INCHES);
    }

    public static double focalLengthAt640(double apparentRadiusPx, double distanceInches,
                                         int frameWidth, double ballDiameterInches) {
        if (apparentRadiusPx <= 0 || distanceInches <= 0 || frameWidth <= 0 || ballDiameterInches <= 0) {
            return Double.NaN;
        }
        double focalAtFrame = (apparentRadiusPx * 2.0 * distanceInches) / ballDiameterInches;
        return focalAtFrame * (CALIBRATION_WIDTH / (double) frameWidth);
    }

    /**
     * Downward pitch that puts a floor ball at {@code floorDistanceInches}
     * on the optical axis. At 19 in height and 31 in tape, this is about 30°.
     */
    public static double suggestedTiltDegrees(double cameraHeightInches, double floorDistanceInches) {
        return suggestedTiltDegrees(cameraHeightInches, floorDistanceInches, DEFAULT_BALL_DIAMETER_INCHES);
    }

    public static double suggestedTiltDegrees(double cameraHeightInches, double floorDistanceInches,
                                             double ballDiameterInches) {
        if (floorDistanceInches <= 1e-6) {
            return Double.NaN;
        }
        double drop = cameraHeightInches - ballDiameterInches / 2.0;
        return Math.toDegrees(Math.atan(drop / floorDistanceInches));
    }

    /**
     * Downward pitch that makes a floor ball seen at image row {@code pixelY}
     * read {@code forwardInches} with floor-plane localization. Unlike
     * {@link #suggestedTiltDegrees}, the ball does not need to be on the
     * optical axis.
     *
     * @param focalPx focal length at the frame the pixel came from (see
     *                {@link LocalizationMath#focalPx})
     */
    public static double tiltForFloorPoint(double pixelY, int frameHeight, double focalPx,
                                           double cameraHeightInches, double forwardInches,
                                           double ballDiameterInches) {
        if (frameHeight <= 0 || focalPx <= 1e-6 || forwardInches <= 1e-6) {
            return Double.NaN;
        }
        double drop = cameraHeightInches - ballDiameterInches / 2.0;
        double aboveAxis = Math.atan2(frameHeight / 2.0 - pixelY, focalPx);
        return Math.toDegrees(aboveAxis + Math.atan2(drop, forwardInches));
    }

    /**
     * Focal length at 640-wide from two floor balls at different taped
     * forward distances. Pair with {@link #tiltForFloorPoint} to fix both
     * tilt and FOV. Returns NaN when the points are too close together in
     * the image to separate focal length from tilt.
     */
    public static double focalForTwoFloorPoints(double pixelY1, double forwardInches1,
                                                double pixelY2, double forwardInches2,
                                                int frameWidth, int frameHeight,
                                                double cameraHeightInches, double ballDiameterInches) {
        if (frameWidth <= 0 || frameHeight <= 0 || forwardInches1 <= 1e-6 || forwardInches2 <= 1e-6) {
            return Double.NaN;
        }
        double a1 = frameHeight / 2.0 - pixelY1;
        double a2 = frameHeight / 2.0 - pixelY2;
        if (Math.abs(a1 - a2) < 0.05 * frameHeight) {
            return Double.NaN;
        }
        double drop = cameraHeightInches - ballDiameterInches / 2.0;
        double target = Math.atan2(drop, forwardInches2) - Math.atan2(drop, forwardInches1);
        // atan(a1/f) - atan(a2/f) peaks at f = sqrt(a1*a2) when both share a sign;
        // start above the peak so the search interval has a single root.
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
        return 0.5 * (lo + hi) * (CALIBRATION_WIDTH / (double) frameWidth);
    }

    /** Predicted apparent radius at a known distance, for checking a focal guess. */
    public static double expectedRadiusPx(double distanceInches, double focalAt640, int frameWidth) {
        return LocalizationMath.expectedRadiusPx(
                DEFAULT_BALL_DIAMETER_INCHES, distanceInches, focalAt640, 70.4, frameWidth);
    }

    /**
     * Horizontal FOV implied by a 640-wide focal length. Useful when converting
     * a tape calibration back into {@link EasyOBJDConfig#horizontalFovDegrees}.
     */
    public static double horizontalFovDegrees(double focalAt640) {
        if (focalAt640 <= 1e-6) {
            return Double.NaN;
        }
        return Math.toDegrees(2.0 * Math.atan((CALIBRATION_WIDTH / 2.0) / focalAt640));
    }
}
