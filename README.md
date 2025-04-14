![MyParty](/src/main/resources/static/images/logo.png)

# MyParty - Event Management System

*I'm a system made for you to manage your parties*

## Overview

MyParty is a comprehensive event management platform built with modern Java technologies. The system allows users to create, manage, and participate in events, with special features for event organizers including ticket management, notifications, and detailed analytics.

## Features

- **User Management**: Registration, authentication and role-based permissions
- **Event Creation & Management**: Full CRUD operations for events
- **Ticket Management**: Purchase, cancellation, and status tracking
- **Real-time Notifications**: SMS notifications via Twilio integration
- **Advanced Search**: Find events by keywords, categories, and dates
- **Reporting & Analytics**: Track event popularity and ticket conversion rates
- **Secure Authentication**: JWT-based security with role-based access control

## System Architecture

MyParty follows a modular architecture pattern with clear separation of concerns:

```kt
app/
├── controller/     # REST API endpoints
├── service/        # Business logic implementation
├── repository/     # Data access layer
├── entity/         # Domain models
├── dto/            # Data transfer objects
├── config/         # Application configurations
├── messaging/      # RabbitMQ integration
├── security/       # Authentication & authorization
├── exception/      # Custom exception handling
└── docker/         # Docker configuration files
```

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.4.2
- **Security**: Spring Security with JWT-based authentication
- **Database**: MySQL
- **Messaging**: RabbitMQ
- **Notifications**: Twilio API
- **Documentation**: Swagger/OpenAPI
- **Containerization**: Docker
- **Testing**: Spring WebFlux Reactive Testing, JUnit, Mockito
- **Validation**: Spring Validation
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Maven

## Installation & Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/rafaelviefe/my-party.git
   cd my-party
   ```

2. Start the required infrastructure services:
   ```bash
   cd app/docker
   docker compose up -d
   ```

3. Build the application:
   ```bash
   mvn clean package
   ```

4. Run the application:
   ```bash
   java -jar target/app-0.0.1-SNAPSHOT.jar
   ```

5. The application will be available at http://localhost:8080

## API Documentation

Comprehensive API documentation is available via Swagger UI once the application is running:

```
http://localhost:8080/swagger-ui.html
```

## Key Components

### User Roles

- **Participant**: Can browse events, purchase tickets, and manage their profile
- **Organizer**: Can create and manage events, view attendee lists, and access analytics
- **Admin**: Has full system access including user role management

### Event Management

Events include key information such as:
- Title and description
- Date and location
- Pricing information
- Category
- Organizer details
- Ratings and reviews

### Ticket Lifecycle

1. **Creation**: When a user selects an event
2. **Pending**: Initial status while payment is being processed
3. **Confirmed**: After successful payment verification
4. **Canceled**: If payment fails or user cancels

### Notification System

The system uses multiple notification channels:
- **SMS Notifications**: via Twilio integration
- **Email Notifications**: for confirmations and updates
- **Scheduled Reminders**: using Spring Scheduler for upcoming events

## Security

MyParty implements robust security measures:
- JWT-based authentication
- Role-based access control
- Endpoint protection based on user permissions
- Secure password storage with bcrypt encoding

## Development

### Running Tests

```bash
mvn test
```

### Code Style and Guidelines

The project follows standard Java coding conventions and Spring best practices:
- Clear separation of concerns
- Descriptive method and variable names
- Comprehensive JavaDoc comments
- Proper exception handling
- Validation for all input data

## Future Enhancements

- Integration with social media platforms
- QR code generation for event sharing
- Enhanced analytics dashboard
- Google Calendar synchronization
- Mobile application support
- Payment gateway integration

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

Developed with ❤️ by **Rafael Vieira Ferreira**
