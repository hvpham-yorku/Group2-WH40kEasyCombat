## For ITR3
## Meeting Minutes

2026.03.16 - After re-establishing the basic logic of automatic combat, the development of the game engine began. The UI style has been determined.

2026.03.23 - Testing of the game and UI revealed that the software was not adapted to the laptop screen, requiring adaptation modifications. The game logic functioned correctly, and development of subsequent custom features began.

2026.03.28 - The custom ability script was reviewed, and the final testing process began.

2026.03.29 - Discuss software optimization and reduce dead code.

## Changes
2026.03.18 - Added CSS files to unify the UI style

2026.03.19 - Improved UI suitability for small screens, and modified some instances to better match the auto battle function.

2026.03.24 - The direct Datasheets CRUD functionality has been removed, replaced by player-defined rules and the ability to import external CSV files to synchronize data with the official database.

2026.03.25 - With the upcoming release of Warhammer 40k edition 11, some interfaces have been left open to allow for potential rule changes in future updates.

2026.03.29 - All dead code and features from the previous version have been completely removed, leaving only the interfaces reserved for future updates.

## Concern
Due to the upcoming update to Warhammer 40,000, which may involve significant rule changes, it is essential to be prepared for the upcoming update.

## Task Assignments
Bin Xu - Custom rules VM

Kishan Kumarasamy - CSS and unit tests

Mark Susanto - Unit tests and Game engine

Ruien Huang - JavaFX UI and Auto-Battle Modifications

San Khoshaba - Game calculation and update UML

## Development Tasks per User Story

---

### Big User Story #1  
### As a user, I want to create and store many Warhammer entities in the system so that I can manage my collection in all one place.

### Detailed Stories
- Search datasheets and core rules by keywords in the database  
  - Dev = San  
  - Tester = Henry  

- Build and validate an army composition  
  - Dev = Ruein  
  - Tester = Mark  

- Import an existing army list from an official source  
  - Dev = Ruien  
  - Tester = Henry  

- Import datasheet data from an official CSV source  
  - Dev = Ruien  
  - Tester = Henry  

- View full details for a selected unit in the database  
  - Dev =  
  - Tester = Henry  

- Filter out datasheets based on parameters like faction  
  - Dev =  
  - Tester = Henry  

---

### Big User Story #2  
### As a user, I want to be able to start a game that can simulate Warhammer battles and log round results so that I can track and simplify game sessions

### Detailed Stories
- Set up a game session and validate parameters such as game mode rules and point limits  
  - Dev = Ruien  
  - Tester = Mark  

- Select overall mission and secondary objectives for a session  
  - Dev =  
  - Tester =  

- Activate stratagems and update command points  
  - Dev = Ruien  
  - Tester =  

- Load an existing army preset during game setup  
  - Dev = Ruien  
  - Tester =  

- Simulate combat between two selected units  
  - Dev = Ruien  
  - Tester = Mark  

- Generate dice results from user selection  
  - Dev = Ruien Huang  
  - Tester =  

- Run the game engine through official battle phases  
  - Dev = Mark  
  - Tester =  

- Apply effects to units and armies  
  - Dev = San  
  - Tester =  

---

### Big User Story #3  
### As a user, I want a system that can keep track of and store its state during sessions so that I know what actions have been performed to get to this point

### Detailed Stories
- Create a battle log system tracking all actions that cause state changes  
  - Dev = Henry  
  - Tester =  

---

### Big User Story #4  
### As a user, I want to be able to make customizable rules so that I can modify and create new ways to play

### Detailed Stories
- Create custom rules by using the GUI tool  
  - Dev = Henry  
  - Tester =  

- Able to import custom rule files from somewhere  
  - Dev = Henry  
  - Tester =  

- Able to modify the custom rule by using the GUI tool  
  - Dev = Ruien  
  - Tester =  

- Able to apply the custom rule  
  - Dev = Henry, Ruien  
  - Tester =  

## Time Spent (days)
Big User Story #1 - Plan 10 - Actual - 6
Big User Story #2 - Plan 16 - Actual -20
Big User Story #3 - Plan 12 - Actual - 8
Big User Story #4 - Plan 7 - Actual - 7

---

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
