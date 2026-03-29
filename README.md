# WH40K EasyCombat

## Project Description

WH40K EasyCombat is a desktop based Warhammer 40,000 battle assistant system developed for EECS 2311 – Software Development Project (Winter 2026).

### Data source of this software powered by [Wahapedia](https://wahapedia.ru/)

The application supports Warhammer 40K players during combat by providing:

- Army management
- Unit and weapon configuration
- Rule browsing and editing
- Combat phase tracking
- Auto-battle function for beginners
- Static data management
- Database-backed persistence

### Special features: [⚔️ Auto battle rules visualization script editing](https://github.com/hvpham-yorku/Group2-WH40kEasyCombat/wiki/Customizable-Rule-System)

---

## 🚀 Download now (Windows EXE installation)
[Download (Windows)](https://github.com/hvpham-yorku/Group2-WH40kEasyCombat/releases/latest)

---

## Documentation

See the full documentation in the [Project Wiki](https://github.com/hvpham-yorku/Group2-WH40kEasyCombat/wiki).

---

## Build & Run Instructions

### Prerequisites

- Java 17+
- Maven 3.8+
- JavaFX properly configured in Eclipse

---

### Before You Run
If you have previously run an older version of this software,
please delete the local application data folder for this app to ensure proper operation.

On Windows, the runtime data now lives under:

    %LOCALAPPDATA%\WH40KEasyCombat

### Run Using Eclipse (JavaFX)

1. Navigate to:

       src/main/java/eecs2311/group2/wh40k_easycombat

2. Right-click `Main.java`
3. Select:

       Run As → Run Configurations → Maven Build

4. In Goals, enter:

       clean javafx:run

5. Click: Apply and Run

---

### Run Using Terminal

From project root:

    mvn clean javafx:run

---

### Run Tests by Terminal

From project root:

    mvn clean test

---

## Windows Packaging

### Prerequisites

- JDK 21+ with `jpackage`
- Maven 3.8+
- For installer `.exe` output: WiX Toolset installed and added to `PATH`
- When building the installer with WiX 6, install the matching `WixToolset.Util.wixext` and `WixToolset.UI.wixext` extensions as well.

### Build a portable Windows app folder

From project root:

    mvn clean package -Pwindows-app-image

Or use the helper script:

    .\scripts\package-windows.ps1

Output:

    dist\app-image\WH40KEasyCombat

Run:

    dist\app-image\WH40KEasyCombat\WH40KEasyCombat.exe

### Build a Windows installer exe

From project root:

    mvn clean package -Pwindows-installer

Or use the helper script:

    .\scripts\package-windows.ps1 -Type exe

Output:

    dist\installer

This installer now lets the user choose the install directory during setup.

### Notes

- The packaged app stores its writable database, CSV overrides, and custom VM rules in `%LOCALAPPDATA%\WH40KEasyCombat`.
- Built-in CSV and DSL files are extracted from the packaged jar into the app's local runtime directory automatically on startup.
- Packaging output is now written to `dist/`, so `mvn clean` used during normal IDE runs does not try to delete the packaged `.exe`.
- The current `jpackage`-based installer supports install-directory selection via `--win-dir-chooser`, but it does not provide a simple built-in option for a custom finish prompt or a "launch app now" checkbox.

