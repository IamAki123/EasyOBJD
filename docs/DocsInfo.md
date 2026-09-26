# EasyOBJD docs

New here? Start with the [README](../README.md). It's one page.

> [!CAUTION]
> **Configure your robot with EasyOBJD Range Test or the distances will be wrong.** [5-minute guide](Tuning.md#1-configure-your-robot-about-5-minutes-required)

| I want to… | Open |
| --- | --- |
| Check webcam / lighting first | [Prerequisites](Prerequisites.md) |
| Add the library to an FTC project | [Install](Install.md) |
| Copy a working TeleOp | [Sample OpMode](SampleOpMode.md) |
| Make distances accurate, or tune colors | [Tuning](Tuning.md) · [copy-in files](../samples/README.md) |
| Look up a class or method | [API](API.md) · [What each file does](LibraryFiles.md) |
| Understand how detection works | [Simple explanation](MathButDumbed.md) · [Math / formulas](Math.md) |
| Fix a blank mask or wrong X/Y | [Troubleshooting](Troubleshooting.md) |

The runtime is `EasyOBJDPipeline` on an EasyOpenCV webcam. Copy-in tools live under [`samples/`](../samples/) and are **not** in the JitPack AAR.
