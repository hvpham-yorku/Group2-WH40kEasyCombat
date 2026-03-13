# WH40K EasyCombat

## Project Description

WH40K EasyCombat is a desktop based Warhammer 40,000 battle assistant system developed for EECS 2311 – Software Development Project (Winter 2026).

### Data source of this software powered by [Wahapedia](https://wahapedia.ru/)

The application supports Warhammer 40K players during combat by providing:

- Army management
- Unit and weapon configuration
- Rule browsing and editing
- Combat phase tracking
- Static data management
- Database-backed persistence
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
please delete the app.db file in the project folder to ensure proper operation.

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

### Run Tests

From project root:

    mvn clean test

---

