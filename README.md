# Smart Agriculture Monitoring System

## About the Project

The **Smart Agriculture Monitoring System** is an Android application developed to monitor environmental sensor data in real-time. With an intuitive and functional interface, the system allows users to visualize information such as temperature, humidity, pressure, and other data collected by distributed sensors.

---

## Features

- **User Authentication**: Secure login with email and password.
- **Pin Visualization on Map**: Each pin on the map represents a location with sensors.
- **Sensor Details**: List of sensors available at each pin with detailed information.
- **Data Simulation**: Sensor values are dynamically updated for demonstration purposes.
- **Profile Management**: Users can view their profiles.

---


### Login Screen
Users enter their email and password for authentication.

**username:** admin@admin.com
---
**password:** admin1
---

<p align="center">
  <img src="screenshots/imagem1.png" alt="Login Screen" width="50%">
</p>

---

### Main Screen (Home)
Displays a list of available pins and allows exploration of the sensors for each pin.

<p align="center">
  <img src="screenshots/imagem2.png" alt="Main Screen" width="50%">
</p>

---

### Map Tab
The **Map Tab** provides a dynamic and interactive map interface to visualize sensor locations.

#### Features:
1. **Pin Display**:
   - Displays pins representing monitored locations fetched from **Firestore**.
   - Each pin corresponds to a unique location with a name (e.g., "House1", "House2").

2. **Sensor Details**:
   - Clicking on a pin opens a bottom sheet dialog displaying:
     - Sensor type (e.g., temperature, humidity).
     - Latest reading value and unit.

3. **Use of ViewModel**:
   - A **ViewModel** is employed to manage and fetch sensor data asynchronously. It ensures separation of concerns and retains data during configuration changes.
   
#### Screenshots:

<p align="center">
  <img src="screenshots/imagem3.png" alt="Map Overview with Pins" width="50%">
</p>

<p align="center">
  <img src="screenshots/imagem4.png" alt="Sensor Details Bottom Sheet" width="50%">
</p>

---

### Profile Screen
Users can view personal information, such as their date of birth and favorite color.

<p align="center">
  <img src="screenshots/imagem5.png" alt="Profile Screen" width="50%">
</p>

---

## Database Structure

The application uses **Firebase Firestore** for data storage.

### Collection user_info
Stores user information:
```json
{ 
    "YwfXhFfGa7gzjMWBZtC2sGGPM9n2": { 
        "color": "green", 
        "dob": "2003-08-15T00:00:00Z" 
    }, 
    "3wtizcG0rBhg6khNODpAJ7zPIW2": { 
        "color": "blue", 
        "dob": "2000-05-22T00:00:00Z" 
    }
}
```

### Collection `pins`
Stores pins and sensor information:
```json
{
  "pins": {
    "2PckZeLnuHYyElh3pxob": {
      "latitude": 40.642704117473436,
      "longitude": -8.651454448699951,
      "pinName": "House2",
      "sensors": [
        {
          "description": "Sensor temperatura Xiaomi",
          "typeSensor": "temperature",
          "unit": "°C",
          "value": [22.5, 23.0, 24.1],
          "timestamp": [1672531200000, 1672534800000, 1672538400000]
        },
        {
          "description": "Sensor de Humidade",
          "typeSensor": "humidity",
          "unit": "%",
          "value": [45.0, 47.5, 50.2],
          "timestamp": [1672531200000, 1672534800000, 1672538400000]
        }
      ]
    },
  }
```

---

## Technologies Used

- **Android SDK**
- **Firebase Firestore** for data storage.
- **Firebase Authentication** for user authentication.
- **OSMDroid** for map rendering.

---

## Project Structure

- **`MainActivity`**: Manages user login.
- **`HelloWorldActivity`**: Controls the tabs (Home, Map, Profile).
- **`HomeFragment`**: Lists pins and sensors.
- **`MapFragment`**: Displays the map with pins and details.
- **`ProfileFragment`**: Shows user profile information.
- **`MapViewModel`**: Manages database operations.

---

## Future Improvements

- Add support for notifications when a sensor reaches critical values.
- Implement integration with external APIs for weather forecasts.
- Enhance the interface with a more modern design.

---

## Authors

This project was developed by the following contributors for the **Mobile Computing** course unit:

- [Sebastian Duque González](https://github.com/ElSebasdg)  
- [Daniel Nascimento Pedrinho](https://github.com/Pedrinho-Dev01)

---
