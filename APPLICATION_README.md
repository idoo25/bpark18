# BPark18 Server GUI Application

This JavaFX application provides a graphical interface for managing the parking server.

## Project Structure

```
bpark18/
├── src/
│   ├── main/java/
│   │   ├── serverGUI/
│   │   │   ├── ServerApp.java          # Main JavaFX Application class
│   │   │   └── ServerPortFrame.java    # GUI Controller
│   │   └── server/
│   │       └── ParkingServer.java      # Server logic
│   └── resources/
│       └── serverGUI/
│           ├── ServerGUI.fxml         # GUI layout
│           └── ServerGUI.css          # Styling
```

## Features

- **Server Management**: Start and stop the parking server
- **Client Monitoring**: Track connected clients
- **Server Status**: View server messages and status updates
- **Clean GUI**: Professional interface with CSS styling

## GUI Components

- **Server IP**: Display and configure server IP address
- **Text Message Area**: Shows server status and log messages
- **Client Connections**: Displays number of connected clients
- **Exit Button**: Safely stops the server and closes the application

## Requirements

- Java 11 or later
- JavaFX 11 or later (included via Maven dependencies)
- Maven for building

## Building and Running

### Build the project:
```bash
mvn compile
```

### Run with JavaFX plugin:
```bash
mvn javafx:run
```

### Alternative run with module path:
```bash
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp target/classes serverGUI.ServerApp
```

## Implementation Details

The application follows JavaFX best practices:

1. **ServerApp** extends `Application` and handles the JavaFX lifecycle
2. **ServerPortFrame** implements `Initializable` and manages GUI controls
3. **ParkingServer** provides the business logic for server operations
4. FXML file defines the UI layout with proper controller binding
5. CSS provides consistent styling across the application

The server automatically starts on port 8080 when the GUI loads, and properly shuts down when the application is closed.