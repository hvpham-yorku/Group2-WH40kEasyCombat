# WH40K EasyCombat

## 1. Project Description

WH40K EasyCombat is a desktop based Warhammer 40,000 battle assistant system developed for EECS 2311 – Software Development Project (Winter 2026).

### Data source of this software powered by Wahapedia

The application supports Warhammer 40K players during combat by providing:

- Army management
- Unit and weapon configuration
- Rule browsing and editing
- Combat phase tracking
- Static data management
- Database-backed persistence

The system follows a layered architecture (Controller → ViewModel → Manager → Service → Repository → Database) and uses a custom lightweight ORM-style database layer.

The frontend is built using JavaFX and the backend logic is implemented in Java with Maven.

---

## 2. Project Structure

### Main Application Code

Location:

    /src/main/java/eecs2311/group2/wh40k_easycombat

Includes:

- `Main.java` – Application entry point
- `annotation/` – Custom ORM-style annotations used for database mapping
- `cell/` – Custom JavaFX UI cells for ListView and TableView rendering
- `controller/` – JavaFX controllers responsible for handling UI interactions
- `db/` – Database infrastructure including connection handling and DAO utilities
- `manager/` – Game state managers (ArmyManager, RoundManager, etc.)
- `model/` – Domain models representing database entities
- `model/instance/` – Runtime instance models used during gameplay
- `repository/` – Data access layer responsible for querying and updating the database
- `service/` – Business logic layer connecting controllers and repositories
- `service/ruleservice/` – Services dedicated to loading and managing game rules
- `tools/` – Internal helper tools used across services
- `util/` – General utility classes
- `viewmodel/` – View models used to connect data models with UI components

---

### UI Resources

Location:

    /src/main/resources/eecs2311/group2/wh40k_easycombat

Includes:

- `MainUI.fxml` – Main menu interface
- `GameUI.fxml` – Main gameplay interface
- `Army.fxml` – Army management interface
- `armyImport.fxml` – Army import interface
- `Datasheets.fxml` – Datasheet viewing interface
- `RuleEditor.fxml` – Rule editing interface
- `UnitAbility.fxml` – Unit ability management interface
- `WeaponEditor.fxml` – Weapon editing interface
- `application.css` – Global application stylesheet

---

### Static Data Resources

Location:

    /src/main/resources/csv

Includes:

- CSV files containing static Warhammer 40k data such as units, weapons, abilities, and keywords used to populate the database.

---

### Database Scripts

Location:

    /src/main/resources/sql

Includes:

- `001_schema.sql` – Database schema definition
- `WarHammer40kRules.json` – Static rule data used to initialize the rules database

---

### Testing

Location:

    /src/test/java

Includes:

- Repository tests
- Service layer tests
- Static data loading tests

All tests can be executed using Maven.
---

### Testing

Location:

    /src/test/java/eecs2311/group2/wh40k_easycombat

Includes:

- Repository tests
- Static data persistence tests
- Test setup utilities

All tests can be run using Maven.

---

## 3. Implemented Features (Current Progress)

### Army Management
- Create and manage armies
- Import armies from external data
- Store army structures including detachments, units, and wargear
- Retrieve army data from the database

### Unit & Weapon Management
- Create and edit units
- Assign melee and ranged weapons
- Manage unit abilities
- Manage unit and weapon keywords
- Display unit datasheets

### Game Combat System
- Game UI for battle management
- Player turn management
- Command Point (CP) tracking
- Round progression and round confirmation
- Battle state management

### Datasheet Viewing
- Search and view unit datasheets
- Display abilities, keywords, and wargear
- Styled text rendering for rule descriptions

### Static Data Service
- Load initial data from CSV files
- Load rule data from JSON files
- Cache static game data for faster access

### Database Layer
- SQLite database integration
- Custom DAO system
- Annotation-based ORM-style mapping
- Transaction management
- Schema initialization using SQL scripts

### Testing
- Repository-level unit tests
- Static data loading tests
- Persistence verification tests

---

## 4. Technologies Used

- Java
- JavaFX
- Maven
- JUnit
- SQL (SQLite)
- Eclipse IDE

---

## 5. Build & Run Instructions

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

## 6. Current Limitations

- Desktop application only
- No authentication system
- UI still under development
- Combat logic not fully automated
- No cloud deployment

---

## 7. Future Improvements

- Expanded rule automation
- Improved UI
- Advanced combat calculations
- Save/load battle states
- Increased test 
- Performance optimizations

---

## 8. Documentation

Includes:

- Planning Document v1.0
- Planning Document v1.5
- Planning Document v1.6
- System architecture diagram
- log file

---
