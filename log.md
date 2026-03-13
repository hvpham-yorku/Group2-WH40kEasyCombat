## For ITR2
## Meeting Minutes
2026.02.23 - Delivery 1 was discussed and it was decided to refactor the database in ITR2.

2026.03.02 -  The Delivery 1 presentation slides were reviewed, and the database reconstruction approach for ITR2, based on the Warhammer CSV data file structure, was finalized.

2026.03.06 - A final testing and debriefing meeting will be held before the end of ITR2.

2026.03.09 - Because the due date was postponed, more code structure optimization tasks were identified.

## Changes
2026.02.26 - The discovery of Data Export from Wahapedia established the basis for reading its CSV files to obtain relevant data and rules.

2026.03.02 - To better handle CSV file input, the database structure was restructured to align with the CSV data structure.

2026.03.03 - The appearance of the `RulesUI` page was redesigned based on the CSV data, and it was renamed to `Datasheets`.

2026.03.04 - The service classes were refactored and several new service classes were added to meet the needs of reading large amounts of data from the database.

2026.03.05 - Most of the features in the Army section have been added; users can now create armies and use some basic logic in the Game section. The virtual dice feature has been removed because more complex new features will be developed for ITR3.

2026.03.10 - The entire software structure was restructured, the functionality of each package and class was defined, overlapping classes and functions were removed, and new classes were added to control the location of functions.

## Concern
The database restructuring required rewriting a significant amount of structural code,
and it resulted in some team members not understanding some new attributes that had been renamed to align CSV data.
Meanwhile, changes to the service class caused errors in some content planned for development in ITR1, and it was postponed to ITR3.

## Task Assignments
Bin Xu - Class structure organization and supervision

Kishan Kumarasamy - Refactor code

Mark Susanto - Write new test cases, write basic game logic

Ruien Huang - Refactor the database and code, modify front-end.

San Khoshaba - Data populator and documentation

## Development Tasks per User Story
### Displays more professional Warhammer data
- Class structure organization
- Refactor code
- Refactor database
- modify front-end
- Data populator for CSV files

### Basic Game Logic
- Write basic game logic
- Available Game UI

### Army table creation
- New Army database
- Army service classes

## Time Spent (days)
Displays more professional Warhammer data - Plan 3 - Actual - 5


Basic Game Logic - Plan 2 - Actual 3


Army table creation - Plan 3 - Actual 1


Restructuring - Plan 2 - Actual 1

---

## For ITR1
## Meeting Minutes
2026.01.26 - The first meeting after ITR0 concluded reaffirmed the use cases and architecture-related tasks selected in ITR0.

2026.02.02 -  With the class structure determined, UI development and data structure establishment began.

2026.02.09 - Due to development schedule constraints, a meeting was held to discuss and modify some tasks and use cases. The main use cases remained unchanged, and a basic game playthrough was added for demonstration purposes after discussions with the professor.

2026.02.13 - Final checks and tests meeting before the ITR1 due date

## Changes
2026.02.02 - The test cases were modified into a more understandable form and divided into corresponding Tasks.

2026.02.03 - The use cases required for ITR1 in the planning document have been modified. The auto-battle feature was removed because its development was incompatible with the development timeline of ITR1, so it was moved to a later date.

2026.02.06 - Some unreasonable tasks have been modified and merged, and tasks have been separated from use cases in Jira for easier visibility.

2026.02.07 - The JDK version issue for JavaFX has been resolved; the version has now been updated from 13 to 17.

2026.02.08 - The file structure within Git has been modified to better conform to standard development workflows.

2026.02.09 - After consulting with the professor in class, we discovered that some basic game demonstration functions were needed. We temporarily removed the army creation function and replaced it with an army view function, and added simple monitoring of CP and round-based gameplay.

2026.02.10 - During data entry testing, it was discovered that two columns were missing from the database. The database was subsequently modified.

2026.02.12 - After performing the final UI connection test, it was found that the automatically generated database read code was missing parentheses, causing the database write to fail. The database read/write generation code was modified and the database was recreated.

## Concern
Since CRUD is the most basic function and one of the main use cases, we didn't know before class on February 9th that the professor wanted us to demonstrate some of the game functionality. This resulted in only 5 days for modification and development, making the development time extremely tight.

## Task Assignments
Bin Xu - Database creation and automatic repository generation

Kishan Kumarasamy - Schema for database and Auto generation

Mark Susanto -  Base classes and automatic generation of and CRUD files

Ruien Huang - Design JavaFX user interface and creation of controllers

San Khoshaba - The database automatic data entry for demo

## Development Tasks per User Story
### Datasheets and Rule Entry
- Implement UI for entering and editing datasheets
- Implement UI for creating and editing rules
- Implement save and update functions for datasheets and rules
- Connect UI with database through repository
- Test data insertion and retrieval

### Basic Simple Rounds and CP Calculations
- Display current round, phase, and CP in the Game UI
- Implement next round and next phase buttons
- Implement CP gain logic
- Test round progression and CP calculation

### Ability Setting
- Implement UI for assigning abilities to units
- Load and display abilities for the selected unit
- Save unit–ability relationships to the database
- Test ability assignment and loading

### Rule (Datasheet) Searching and Viewing
- Implement search function for datasheets and rules
- Implement filtering by name or faction
- Implement a detailed view page for datasheets
- Test search and display functions

### Army List
- Units can assign to army
- Test army list creation and loading

## Time Spent (days)
Datasheets and Rule Entry - Plan 15 - Actual - 18

Basic Simple Rounds and CP Calculations - Plan 2 - Actual 1


Ability Setting - Plan 7 - Actual 6


Rule (Datasheet) Searching and Viewing - Plan 17 - Actual 14


Army List - Plan 3 - Actual 3
