# DrivenByMoss4Reaper — tmx mod (unofficial)

Configurable Reaper mixer scrolling when selecting off-screen tracks from a Mackie/MCU controller.

Unofficial modified build of [DrivenByMoss4Reaper](https://www.mossgrabers.de/Software/Reaper/Reaper.html)
by Jürgen Moßgraber (LGPLv3). Not supported by the original author.

Current version: **26.6.5-tmx2** (based on DrivenByMoss 26.6.5)

## Changes
- **Mixer scroll on track select** (Mackie/MCU controllers): new setting
  *Reaper Mixer (tmx mod) → Scroll mixer on track select*:
  - Always snap to left edge (original behaviour)
  - Only if hidden: snap to left edge (default)
  - Only if hidden: scroll just into view
  - Never scroll the mixer
- Build identification: window title, jar name, control surface description and
  DLL properties show the tmx version.

All changes are marked `[tmx mod]` in the source.

## Layout
- `reaper_drivenbymoss/`, `CMake/` … C++ backend (`reaper_drivenbymoss.dll`), from
  [DrivenByMoss4Reaper-Backend](https://github.com/git-moss/DrivenByMoss4Reaper-Backend)
- `java/` … Java part (`DrivenByMoss4Reaper-<version>.jar`), from DrivenByMoss4Reaper
- `.github/workflows/` … GitHub builds both on every push. When main contains a new tmx version (see `TmxVersion.h`), a Release with both files is published automatically — download from the Releases page.

## Install (Windows)
Close Reaper. Replace `UserPlugins\reaper_drivenbymoss.dll` and put the jar into
`UserPlugins\drivenbymoss-libs` — make sure only **one** `DrivenByMoss4Reaper-*.jar`
is in that folder (rename backups to e.g. `*.jar.original`).

## Updating to a new DrivenByMoss release
1. Bring in Moss's new backend and Java sources, re-apply the `[tmx mod]` changes.
2. Bump the tmx number in `reaper_drivenbymoss/TmxVersion.h`, the 4th number of the
   project VERSION in `CMake/CMakeLists.txt`, and `version` in `java/build.gradle`.
3. Use the backend source matching the installed Java release.

Shelved experiment (QCon function-button LEDs, LED probe): branch `experimental-button-leds`.
The QCon Pro X's F1–F8 LEDs cannot be controlled by the host.
