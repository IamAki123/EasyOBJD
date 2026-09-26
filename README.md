# EasyOBJD

**FTC object detection, simplified.** Point a webcam at game pieces, get how far away they are in inches.

[![Release](https://img.shields.io/github/v/tag/IamAki123/EasyOBJD?label=release)](https://github.com/IamAki123/EasyOBJD/tags)
[![JitPack](https://jitpack.io/v/IamAki123/EasyOBJD.svg)](https://jitpack.io/#IamAki123/EasyOBJD)
[![Tests](https://github.com/IamAki123/EasyOBJD/actions/workflows/tests.yml/badge.svg)](https://github.com/IamAki123/EasyOBJD/actions/workflows/tests.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

<p align="center"><img src="docs/images/configure-your-robot.svg" alt="CONFIGURE YOUR ROBOT OR THE DISTANCES WILL BE WRONG" width="100%"></p>

> [!CAUTION]
> **Do step 3 below before you trust any inches.** The camera numbers that ship with the samples are from another team's robot. It takes about 5 minutes with a tape measure.

## What you get

- **Finds every game piece** of one color, and groups touching pieces into clusters.
- **Tells you where each one is**: inches ahead of your robot and inches left/right.
- **Tunes on the Driver Station**: widen or tighten the color filter with the D-pad, no rebuilding.

## Quick start

### 1. Install

In the **root** `build.dependencies.gradle`, add JitPack to `repositories`:

```gradle
maven { url = 'https://jitpack.io' }
```

In `TeamCode/build.gradle`, add to `dependencies`:

```gradle
implementation 'org.openftc:easyopencv:1.7.3'
implementation 'com.github.IamAki123:EasyOBJD:1.0.3'
```

Then **File → Sync Project with Gradle Files**. Stuck? See [Install](docs/Install.md).

### 2. Copy 3 files into TeamCode

| File | What it is |
| --- | --- |
| [`EasyOBJDUserConfig.java`](samples/EasyOBJDUserConfig.java) | Your robot's settings. The only file you edit. |
| [`EasyOBJDRangeTest.java`](samples/EasyOBJDRangeTest.java) | Measures your camera setup. Run once per robot. |
| [`EasyOBJDCluster.java`](samples/EasyOBJDCluster.java) | Shows every cluster and how far away it is. |

Set `WEBCAM_NAME` in `EasyOBJDUserConfig` to your webcam's name in **Configure Robot** (usually `Webcam 1`).

### 3. Configure your robot (5 minutes)

1. Tape **floor → center of the lens**. Put it in `CAMERA_HEIGHT_INCHES`.
2. Tape **lens → front of the robot**. Put it in `CAMERA_BEHIND_FRONT_INCHES`.
3. Run **EasyOBJD Range Test**. Put a ball ~2 ft ahead, set **Target** to your tape, press **A**.
4. Move the ball ~2 ft farther, set **Target**, press **A** again.
5. Copy the two numbers it shows into `EasyOBJDUserConfig`.

Full walkthrough and common mistakes: [Configure your robot](docs/Tuning.md#1-configure-your-robot-about-5-minutes-required).

### 4. Run it

Run **EasyOBJD Cluster**. Each cluster shows as:

```
#1   29.0 in ahead   2.1 in right
```

Put a tape measure down and check it. If there are no clusters, press **D-pad up** to widen the color filter.

## Use it in your own OpMode

```java
EasyOBJDPipeline pipeline = EasyOBJD.createPipeline(EasyOBJDUserConfig.create());
webcam.setPipeline(pipeline);

for (ClusterInfo cluster : pipeline.getClusters()) {
    double ahead = cluster.y;   // inches forward of the lens
    double right = cluster.x;   // inches right of the lens (negative = left)
}
```

Subtract `CAMERA_BEHIND_FRONT_INCHES` from `cluster.y` to get distance from the robot front. Every method: [API](docs/API.md).

## Need help?

| I want to… | Read |
| --- | --- |
| Fix wrong distances, a blank mask, or a crash | [Troubleshooting](docs/Troubleshooting.md) |
| Tune the color filter or re-measure the camera | [Tuning](docs/Tuning.md) |
| See how it works, no math | [Simple explanation](docs/MathButDumbed.md) |
| Look up a method | [API](docs/API.md) |
| See everything | [All docs](docs/DocsInfo.md) · [Changelog](CHANGELOG.md) · [Contributing](CONTRIBUTING.md) |

**Good to know:** samples are not inside the JitPack library, so you always copy them. EasyOBJD gives camera-relative positions; it does not replace odometry. Accuracy is usually within a couple of inches at 2–4 ft once your robot is configured.

## Credits

Akash Vijay Aradhya — #23918 Super Sigma Robotics
Aditi Rao — #23918 Super Sigma Robotics

AI tools (Cursor, ChatGPT, OpenAI Codex in Cursor) were used as development assistants for code generation, debugging, documentation, and refinement. Architecture, requirements, testing, validation, and final implementation decisions were directed and reviewed by the author.
