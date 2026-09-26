# Sample OpMode

[README](../README.md) · [Tuning](Tuning.md) · [API](API.md)

Copy these into TeamCode (`org.firstinspires.ftc.teamcode`):

> [!CAUTION]
> **Configure your robot with EasyOBJD Range Test or the distances will be wrong.** [How](Tuning.md#1-configure-your-robot-about-5-minutes-required)

| File | Required? |
| --- | --- |
| [`EasyOBJDCluster.java`](../samples/EasyOBJDCluster.java) | Yes — 12×12 grid overlay, D-pad HSV, cluster X/Y |
| [`EasyOBJDUserConfig.java`](../samples/EasyOBJDUserConfig.java) | Yes — your camera height, tilt, focal, HSV, ball size |
| [`EasyOBJDRangeTest.java`](../samples/EasyOBJDRangeTest.java) | Yes, once per robot — solves tilt and focal from a tape measure |
| [`EasyOBJDTuner.java`](../samples/EasyOBJDTuner.java) | Optional — MASK preview while nudging HSV |
| [`EasyOBJDCalibrateSample.java`](../samples/EasyOBJDCalibrateSample.java) | Optional — tape focal / tilt |

Samples are **not** in the JitPack AAR.

## `init()`

```java
pipeline = EasyOBJD.createPipeline();
webcam.setPipeline(pipeline);
webcam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT, OpenCvWebcam.StreamFormat.MJPEG);
```

Set `WEBCAM_NAME` in the sample to match Configure Robot.

## `loop()`

```java
if (gamepad1.dpadUpWasPressed()) {
    pipeline.adjustHsvRange(1);
}
if (gamepad1.dpadDownWasPressed()) {
    pipeline.adjustHsvRange(-1);
}

List<ClusterInfo> clusters = pipeline.getClusters();
for (ClusterInfo cluster : clusters) {
    // cluster.x = inches right of the lens
    // cluster.y = inches forward of the lens
}
```

## First-run checklist

1. `WEBCAM_NAME` matches Configure Robot.
2. Tape camera height and lens-to-front into `EasyOBJDUserConfig`, then run **EasyOBJD Range Test** and paste the tilt / focal it solves.
3. Run **EasyOBJD Cluster**. D-pad up = wider HSV, down = tighter. Left/right cycles MASK / GRID / FULL.
4. Compare telemetry `from robot front` to a tape measure.

Full listings: [`samples/`](../samples/).
