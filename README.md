
# GeoQuizApp

Second attempt at git config for Android Studio project about my SMS-based ride-hailing Android application.

## Overview

GeoQuizApp is an SMS-based ride-hailing Android application designed with offline capabilities and a modular architecture. It provides role-based access for riders and users, enabling them to interact through a chat interface, share locations via SMS, and log locations offline. The app leverages modern Android development practices such as Hilt/Dagger for dependency injection and Room for local data storage.

## Key Features

- **Role-Based Access**: Separate flows for riders and users, each with unique UIs and permissions.
- **SMS-Based Location Sharing**: Allows sharing of locations even in offline scenarios.
- **Offline Location Logging**: Utilizes OSMDroid maps to log and view locations without an internet connection.
- **Room-Based Local Storage**: Stores messages and location logs locally using Room.
- **Chat Messaging Interface**: Supports conversations between riders and users through a streamlined chat interface.
- **Dependency Injection**: Implements Hilt/Dagger for clean architecture and better code maintainability.
- **RecyclerView Adapters**: Optimized for displaying chat messages, history, and map data.
- **Permissions Handling**: Robust handling of SMS and location permissions.
- **Modular Architecture**: Built with well-defined ViewModels, Repositories, and DAOs for scalability and maintainability.

## Getting Started

### Prerequisites

1. Install [Android Studio](https://developer.android.com/studio) on your system.
2. Ensure you have a physical or virtual Android device with SMS capabilities.

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Dzeline/GeoQuizApp.git
   cd GeoQuizApp
   ```

2. Open the project in Android Studio.

3. Sync the project with Gradle files:
   - Go to `File` > `Sync Project with Gradle Files`.

4. Configure SMS and Location Permissions:
   - Ensure that your device or emulator has the required permissions enabled for SMS and location.

5. Build and Run:
   - Click on the `Run` button in Android Studio to build the project and deploy it to your connected device or emulator.

## Architecture

GeoQuizApp follows a modular architecture pattern to ensure scalability and maintainability:

- **ViewModels**: Responsible for managing UI-related data and business logic.
- **Repositories**: Act as a single source of truth for data, whether local or remote.
- **DAOs (Data Access Objects)**: Provide an abstraction layer for database interactions.

## Libraries Used

- **Hilt/Dagger**: For dependency injection.
- **Room**: For local database storage.
- **OSMDroid**: For offline map rendering and location logging.
- **RecyclerView**: For efficient and dynamic UI lists.
- **AndroidX Libraries**: For modern Android development practices.

## Contributing

Contributions are welcome! If you'd like to contribute to the project, please follow these steps:

1. Fork the repository.
2. Create a new branch for your feature or bugfix:
   ```bash
   git checkout -b feature-name
   ```
3. Commit your changes and push them to your fork:
   ```bash
   git commit -m "Description of changes"
   git push origin feature-name
   ```
4. Open a pull request on the main repository.

## License

This project is licensed under the [MIT License](LICENSE).

## Contact

For any questions or feedback, please reach out to the repository owner.
