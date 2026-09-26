/*
 * Copyright (c) 2026 Akash Vijay Aradhya
 *
 * SPDX-License-Identifier: MIT
 *
 * EasyOBJD - FTC EasyOpenCV object detection
 * https://github.com/IamAki123/EasyOBJD
 */
package org.firstinspires.ftc.easyobjd;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EasyOBJDCalibrationTest {

    @Test
    public void focalRoundTrip() {
        double distance = 31.0;
        double diameter = 2.8;
        double focalAt640 = 454.0;
        double radius = (diameter * focalAt640) / (2.0 * distance);
        double recovered = EasyOBJDCalibration.focalLengthAt640(radius, distance, 640, diameter);
        assertEquals(focalAt640, recovered, 0.01);
    }

    @Test
    public void focalScalesFrom320Frame() {
        double distance = 31.0;
        double diameter = 2.8;
        double focalAt640 = 454.0;
        double radius320 = (diameter * focalAt640 * 0.5) / (2.0 * distance);
        double recovered = EasyOBJDCalibration.focalLengthAt640(radius320, distance, 320, diameter);
        assertEquals(focalAt640, recovered, 0.01);
    }

    @Test
    public void suggestedTiltAbout30Degrees() {
        double tilt = EasyOBJDCalibration.suggestedTiltDegrees(19.0, 31.0);
        assertEquals(29.6, tilt, 1.0);
    }

    @Test
    public void fovFromFocalAgreesWithDefault() {
        double fov = EasyOBJDCalibration.horizontalFovDegrees(
                LocalizationMath.focalPx(0, 70.4, 640));
        assertEquals(70.4, fov, 0.05);
    }

    /** Image row where a floor ball at {@code forward} appears for a known mount. */
    private static double floorPixelY(double forward, double tiltDeg, double focalPx,
                                      int frameHeight, double cameraHeight, double diameter) {
        double drop = cameraHeight - diameter / 2.0;
        double aboveAxis = Math.toRadians(tiltDeg) - Math.atan2(drop, forward);
        return frameHeight / 2.0 - focalPx * Math.tan(aboveAxis);
    }

    @Test
    public void tiltForFloorPointRecoversMountOffAxis() {
        double focal = LocalizationMath.focalPx(0, 70.4, 640);
        double py = floorPixelY(45.0, 20.0, focal, 480, 19.0, 2.8);
        double tilt = EasyOBJDCalibration.tiltForFloorPoint(py, 480, focal, 19.0, 45.0, 2.8);
        assertEquals(20.0, tilt, 1e-9);

        double[] floor = LocalizationMath.pixelToFloor(320, py, 640, 480, 19.0, 2.8, 0, 70.4, tilt);
        assertEquals(45.0, floor[2], 1e-6);
    }

    @Test
    public void wrongTiltReadsShortAtRange() {
        // A 5 degree tilt error at 19 in height is enough to read a 45 in ball near 36 in.
        double focal = LocalizationMath.focalPx(0, 70.4, 640);
        double py = floorPixelY(45.0, 20.0, focal, 480, 19.0, 2.8);
        double[] floor = LocalizationMath.pixelToFloor(320, py, 640, 480, 19.0, 2.8, 0, 70.4, 25.0);
        assertEquals(35.4, floor[2], 1.0);
    }

    @Test
    public void focalForTwoFloorPointsRecoversFocalAndTilt() {
        double trueFocalAt640 = 600.0;
        double trueTilt = 22.0;
        double py1 = floorPixelY(24.0, trueTilt, trueFocalAt640 / 2.0, 240, 19.0, 2.8);
        double py2 = floorPixelY(48.0, trueTilt, trueFocalAt640 / 2.0, 240, 19.0, 2.8);

        double focalAt640 = EasyOBJDCalibration.focalForTwoFloorPoints(
                py1, 24.0, py2, 48.0, 320, 240, 19.0, 2.8);
        assertEquals(trueFocalAt640, focalAt640, 0.01);

        double focalPx = LocalizationMath.focalPx(focalAt640, 70.4, 320);
        double tilt = EasyOBJDCalibration.tiltForFloorPoint(py2, 240, focalPx, 19.0, 48.0, 2.8);
        assertEquals(trueTilt, tilt, 1e-6);
    }

    @Test
    public void focalForTwoFloorPointsRejectsPointsTooCloseInImage() {
        assertTrue(Double.isNaN(EasyOBJDCalibration.focalForTwoFloorPoints(
                300, 40.0, 302, 41.0, 640, 480, 19.0, 2.8)));
    }

    @Test
    public void invalidInputsAreNaN() {
        assertTrue(Double.isNaN(EasyOBJDCalibration.focalLengthAt640(0, 31, 640)));
        assertTrue(Double.isNaN(EasyOBJDCalibration.suggestedTiltDegrees(19, 0)));
    }
}
