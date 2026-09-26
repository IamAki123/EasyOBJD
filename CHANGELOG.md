# Changelog

## Unreleased

## 1.0.3

Distances are now accurate once you configure your robot. Run **EasyOBJD Range Test** after upgrading.

### Added
- `EasyOBJDCalibration.tiltForFloorPoint` solves camera tilt from where a taped ball actually appears in the image, and `focalForTwoFloorPoints` solves focal length (FOV) from two taped distances.
- [`EasyOBJDRangeTest`](samples/EasyOBJDRangeTest.java) TeleOp: tape from the robot front to the ball, press A at two distances, and it solves tilt / focal. Readouts are distance from the robot front.
- `EasyOBJDUserConfig.CAMERA_BEHIND_FRONT_INCHES` (lens to front of robot).
- "Configure your robot" guide in [Tuning](docs/Tuning.md) with a required-setup warning in the README.

### Changed
- `EasyOBJDCluster` reads camera height, tilt, focal, and ball diameter from `EasyOBJDUserConfig` and shows each cluster's distance from the robot front. Copy both files.
- Sample mount defaults are now a measured robot (18 in, 15.58°, focal 710.4 at 640) instead of 19 in / 25° / FOV-derived focal. Teams must still run Range Test on their own robot.

### Fixed
- Calibrate sample no longer assumes the ball sits at image center when suggesting `CAMERA_TILT_DEGREES`. That assumption put tilt several degrees off, which reads balls short at range (about 36 in for a 45 in ball at 19 in height).

## 1.0.2

### Changed
- First-run sample is [`EasyOBJDCluster`](samples/EasyOBJDCluster.java): 12×12 grid overlay, D-pad HSV, left/right overlay cycle. Replaces `EasyOBJDSample`.

## 1.0.1

### Changed
- Tiny public types: `OverlayMode` (top-level, same as JitPack 1.0.0), `EasyOBJD.LocalizationMethod`, `EasyOBJD.IntakeHeuristic`, `ClusterInfo.Ball`. Grid grouping moved into `LocalizationMath.java`.
- Removed `EasyOBD*` compatibility classes. Use `EasyOBJD` / `EasyOBJDPipeline` / `EasyOBJDConfig` / `EasyOBJDCalibration`.
- Java package renamed from `org.firstinspires.ftc.easyobd` to `org.firstinspires.ftc.easyobjd`. Update TeamCode imports.
- Sample workflow matches the first release: `EasyOBJD.createPipeline()`, D-pad HSV, cluster X/Y in one OpMode. UserConfig / Tuner / Calibrate are optional.

## 1.0.0

First EasyOBJD release. Project renamed from EasyOBD. Java package stays `org.firstinspires.ftc.easyobd`. JitPack: `com.github.IamAki123:EasyOBJD:1.0.0`.

### Added
- TeamCode copy-in [`EasyOBJDUserConfig`](samples/EasyOBJDUserConfig.java), [`EasyOBJDTuner`](samples/EasyOBJDTuner.java) (D-pad HSV), and Calibrate sample.
- Explicit library config for HSV (`.hsv(...)`) and ball size (`.ballSize(...)`).
- Color-agnostic HSV nudge.
- EasyATL-style docs (Install, Tuning, API, math, contributing, security), MIT file headers, issue templates, and CI.
- `KnownGeometries` / `SampleValuesTest` for the published sample defaults.

### Changed
- Primary types: `EasyOBJD`, `EasyOBJDPipeline`, `EasyOBJDConfig`, `EasyOBJDCalibration`.
- `EasyOBD*` names remain as deprecated aliases.
- Sample documents `WEBCAM_NAME` vs the Robot Controller configuration.

## 0.2

Config, API, and robustness pass. Detection math is the same idea (HSV → grid → arc split → floor or size) with clearer contracts and fewer shared statics.

### Added
- `EasyOBDConfig` builder / per-pipeline tunables (camera, HSV, grid, overlay, smoothing, extra color ranges).
- `EasyOBDCalibration` plus `samples/EasyOBDCalibrateSample.java` for tape focal-length and tilt.
- Richer `ClusterInfo` / top-level `Ball`: range, ball list, circularity, confidence, pixel radius, `LocalizationMethod`, tightness, field X/Y.
- `getBestClusterForIntake`, overlay modes, `adjustHsvRange`, `addTelemetry`, `getLastProcessTimeMs` / error helpers.
- Optional adaptive lighting, process width independent of preview, temporal smoothing, HoughCircles verification.
- JUnit tests for pinhole + tilt math, grid clustering, and calibration helpers.
- Expanded README (coordinate frames, calibration, performance, limitations).

### Changed
- Pipeline class is `EasyOBDPipeline`. `EasyOBDCreation` remains as a deprecated subclass.
- `EasyOBD.createPipeline()` returns `EasyOBDPipeline` and accepts an optional `EasyOBDConfig`.
- `getClusters()` / `getBalls()` publish immutable snapshots (copy-on-write).
- Camera statics are defaults only; live tuning should go through `pipeline.getConfig()`.
- Sample shows camera + field usage, debug telemetry, and configurable stream / process size.
- Library version `0.2`. EasyOpenCV remains `1.7.3` (1.7.x).

### Fixed
- Leftmost-cluster floor-vs-size flag, size-based range, and pixel radius are actually published.
- Small / extreme-aspect process frames no longer assume a 12×12 grid.
- Contour `Mat`s from hole-fill and ball find are released each frame.
