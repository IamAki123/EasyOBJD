# Sample OpMode

[README](../README.md) · [Tuning](Tuning.md) · [API](API.md)

Copy [`EasyOBJDCluster.java`](../samples/EasyOBJDCluster.java) into TeamCode (`org.firstinspires.ftc.teamcode`). That is the whole first-run path.

| File | Required? |
| --- | --- |
| [`EasyOBJDCluster.java`](../samples/EasyOBJDCluster.java) | Yes — 12×12 grid overlay, D-pad HSV, cluster X/Y |
| [`EasyOBJDUserConfig.java`](../samples/EasyOBJDUserConfig.java) | Optional — saved HSV, ball size, camera inches |
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
2. Run **EasyOBJD Cluster**. D-pad up = wider HSV, down = tighter. Left/right cycles MASK / GRID / FULL.
3. Compare telemetry `Y` to a tape measure. Optional Calibrate if inches are off.

Full listings: [`samples/`](../samples/).
