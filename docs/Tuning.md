# Tuning

[README](../README.md) · [Troubleshooting](Troubleshooting.md)

$${\color{red}\LARGE\textbf{CONFIGURE YOUR ROBOT OR THE DISTANCES WILL BE WRONG}}$$

> [!CAUTION]
> **EasyOBJD can't guess where your camera is.** Until you do section 1 on *your* robot, distances can be off by a foot or more. Being just 1° off on camera tilt moves a ball 4 ft away by about 2 in.

Two things to tune, in this order:

1. **Camera setup** — so the inches are right.
2. **Color filter (HSV)** — so it sees the pieces.

## 1. Configure your robot (about 5 minutes, required)

**You need:** a tape measure, one ball, and `EasyOBJDUserConfig` + `EasyOBJDRangeTest` copied into TeamCode.

### Measure two things

Type these into `EasyOBJDUserConfig`:

| Setting | Measure from → to |
| --- | --- |
| `CAMERA_HEIGHT_INCHES` | Floor → **center of the lens** |
| `CAMERA_BEHIND_FRONT_INCHES` | Lens → **front of the robot** (along the floor) |

Also check `BALL_DIAMETER_INCHES` matches your real game piece.

### Let Range Test solve the rest

Run **EasyOBJD Range Test**. The top line always tells you what to do next.

1. Put a ball **straight ahead**, about 2 ft from the front of the robot.
2. Tape from the **front of the robot** to the **near edge of the ball**.
3. D-pad until **TARGET** matches your tape. **Do this before pressing A.**
4. Press **A** and hold still.
5. Move the ball about 2 ft farther. Tape, set TARGET, press **A**.
6. Copy the two lines under **PASTE INTO EasyOBJDUserConfig**. Rebuild.

That's it. Run **EasyOBJD Cluster** and check a few distances against the tape.

| Button | Does |
| --- | --- |
| D-pad up / down | Target ±1 in |
| D-pad right / left | Target ±6 in |
| A | Capture |
| B | Start over |
| Bumpers | Color filter wider / tighter |
| X | Change preview |

<details>
<summary>Example numbers from one robot (don't copy these)</summary>

```java
public static double CAMERA_HEIGHT_INCHES = 18.0;
public static double CAMERA_BEHIND_FRONT_INCHES = 6.5;
public static double CAMERA_TILT_DEGREES = 15.58;
public static double FOCAL_LENGTH_PIXELS_AT_640 = 710.4;
```

</details>

### If it's still off

| What happened | Fix |
| --- | --- |
| Pressed A before setting TARGET | Press **B**, set TARGET, press A |
| Taped from the lens, not the robot front | TARGET is always **robot front → near edge of ball** |
| Taped height to the top of the camera | Re-tape to the center of the lens |
| Good up close, bad far away | Do the second capture farther out |
| Camera got bumped or moved | Run Range Test again |

## 2. Color filter (HSV)

Run **EasyOBJD Cluster**.

| Button | Does |
| --- | --- |
| D-pad up | Wider — sees more (use when pieces are missed) |
| D-pad down | Tighter — sees less (use when the floor lights up) |
| D-pad left / right | Change preview (MASK shows only what it sees) |

When the MASK preview shows clean, solid pieces, copy the **HSV low / high** numbers from telemetry into `H_LOW`…`V_HIGH` in `EasyOBJDUserConfig`. Rebuild.

<details>
<summary>Notes for other colors</summary>

- Hue is OpenCV's 0–179 scale, not 0–360.
- Red wraps around 0/179, so it needs two ranges (`extraColorRange` inside `create()`).
- **EasyOBJD Tuner** is an optional OpMode that shows only the mask while you adjust.

</details>

## Other tools

- **EasyOBJD Calibrate** — older one-distance version of Range Test. Range Test is easier and more accurate.
- **Jittery numbers while driving?** Set `smoothingAlpha` to about 0.3 on the library config (0 = raw).

More fixes: [Troubleshooting](Troubleshooting.md) · How it works: [Simple explanation](MathButDumbed.md) · [Formulas](Math.md)
