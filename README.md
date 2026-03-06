# WH40K EasyCombat

## 1. Project Description

WH40K EasyCombat is a desktop based Warhammer 40,000 battle assistant system developed for EECS 2311 – Software Development Project (Winter 2026).

The application supports Warhammer 40K players during combat by providing:

- Army management
- Unit and weapon configuration
- Rule browsing and editing
- Combat phase tracking
- Static data management
- Database-backed persistence

The system follows a layered architecture (Controller → Service → Repository → Database) and uses a custom lightweight ORM-style database layer.

The frontend is built using JavaFX and the backend logic is implemented in Java with Maven.

---

## 2. Project Structure

### Main Application Code

Location:

    /src/main/java/eecs2311/group2/wh40k_easycombat

Includes:

- `Main.java` – Application entry point
- `controller/` – JavaFX controllers
- `model/` – Domain models (Units, Weapons, Armies, etc.)
- `repository/` – Data access layer
- `service/` – Business logic layer
- `db/` – Custom database layer
- `annotation/` – Custom annotations
- `util/` – Utility classes

---

### UI Resources

Location:

    /src/main/resources/eecs2311/group2/wh40k_easycombat

Includes:

- FXML UI layouts
- Stylesheets
- Multiple UI screens (Army UI, Game UI, Rule Editor, Weapon Editor, etc.)

---

### Database Scripts

Location:

    /src/main/resources/sql

Includes:

- `001_schema.sql` – Database schema
- `002_seed.sql` – Seed data

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
- Store army data in database

### Unit & Weapon Management
- Create and edit units
- Assign melee and ranged weapons
- Manage unit keywords
- Manage weapon keywords

### Rule Editing
- Rule editor interface
- Ability management

### Combat System
- Battle state tracking
- Combat phase tracking
- Player turn management

### Static Data Service
- Loads seed data
- Handles persistent game data

### Database Layer
- Custom DAO system
- Annotation-based mapping
- Transaction management
- Schema + seed SQL support

### Testing
- Repository-level unit tests
- Static data persistence tests

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
- Meeting log

---