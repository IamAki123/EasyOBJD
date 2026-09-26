# What each file does

[README](../README.md) · [API](API.md) · [Docs index](DocsInfo.md)

Java package is `org.firstinspires.ftc.easyobjd`.

## You call these

| File | Role |
| --- | --- |
| `EasyOBJD.java` | Factory plus nested `LocalizationMethod` and `IntakeHeuristic`. |
| `OverlayMode.java` | Preview: `FULL`, `MASK`, `GRID`, `BALLS`, `DISTANCES`. |
| `EasyOBJDPipeline.java` | EasyOpenCV pipeline: mask, grid, balls, localize, overlay, snapshots. |
| `EasyOBJDConfig.java` | Library tunables. TeamCode `EasyOBJDUserConfig` fills these with your robot's measured camera mount. |
| `EasyOBJDCalibration.java` | Tape helpers: focal length and suggested tilt. |
| `ClusterInfo.java` | Immutable published detections, including nested `ClusterInfo.Ball`. |

## Internals (tested without a camera)

| File | Role |
| --- | --- |
| `LocalizationMath.java` | Pinhole, tilt, floor hit, robot/field, plus package-private grid grouping. |

## Copy-in (not in the JitPack AAR)

| File | Role |
| --- | --- |
| `samples/EasyOBJDCluster.java` | First-run TeleOp: 12×12 grid overlay, D-pad HSV, cluster X/Y. |
| `samples/EasyOBJDUserConfig.java` | Your camera height, lens-to-front, tilt, focal, HSV, ball size, webcam name. |
| `samples/EasyOBJDRangeTest.java` | Configure your robot: tape two distances, solves tilt and focal. |
| `samples/EasyOBJDTuner.java` | Optional MASK overlay while nudging HSV. |
| `samples/EasyOBJDCalibrateSample.java` | Optional tape focal / tilt. |

See [samples/README.md](../samples/README.md).
