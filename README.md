# Student Database System

A JavaFX application that provides a user interface for managing student records in a MySQL database.

## Implemented Features

### Core Functionality
- View, add, edit, and delete student records in the database
- Student data includes ID, First Name, Last Name, Department, Major, Email, and Image URL
- TableView display with sortable columns for viewing all records

### User Interface
- Interface with separate panels for data entry and display
- Form fields with appropriate labels and validators
- Status bar that displays operation results and validation messages
- Profile image display and selection capability

### Data Validation
- Real-time field validation using regex patterns
- Form fields are validated as users type
- Appropriate feedback for validation errors in the status bar
- Add button is automatically disabled when form fields contain invalid data

### Database Integration
- MySQL database connectivity
- Secure connection using parameterized queries
- Automatic database and table creation if not present
- Error handling for database operations

### User Authentication
- Login screen with username and password validation
- Signup screen for new user registration
- Password requirements enforcement
- User credentials stored in Java Preferences

### Theming Options
- Light theme (default)
- Dark theme option available in the Theme menu

### Data Import/Export
- CSV file import functionality via Data menu
- CSV export for backing up or transferring records
- Standard CSV format with headers

### Session Management
- Thread-safe user session implementation
- User session tracks current user and privileges
- User preferences maintained between sessions

### Menu System
- Full-featured menu bar with File, Edit, Theme, Data, and Help menus
- Context-sensitive menu items (disabled when not applicable)

## Technical Details

### Architecture
- JavaFX for UI components
- FXML for layout definition
- CSS for styling


## Getting Started

### Prerequisites
- Java JDK 11 or higher
- MySQL database
- Maven (for dependency management)

### Setup
1. Clone this repository
2. Configure MySQL connection in DbConnectivityClass.java if needed
3. Compile and run the application
4. Sign up for a new account or log in with existing credentials


## License
This project is for educational use only

