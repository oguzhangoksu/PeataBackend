# PEATA Backend

## Project Overview

PEATA Backend is a robust Spring Boot application designed to support a pet adoption and lost pet reporting system. This project aims to connect pet owners, potential adopters, and community members to facilitate pet adoptions and help reunite lost pets with their owners.

## Technologies Used

- **Java 17**: The core programming language used for the backend development.
- **Spring Boot 3.3.3**: Provides the foundation for creating stand-alone, production-grade Spring-based Applications.
- **Spring Security**: Handles authentication and authorization.
- **Spring Data JPA**: Simplifies data access layer, implementing JPA based repositories.
- **PostgreSQL**: The primary database for storing application data.
- **AWS S3**: Used for storing and retrieving images related to pets.
- **RabbitMQ**: Implements message queuing for the notification system.
- **Thymeleaf**: Server-side Java template engine for email templates.
- **Swagger/OpenAPI**: API documentation and testing.
- **JWT (JSON Web Tokens)**: For secure authentication and information exchange.
- **Lombok**: Reduces boilerplate code in Java classes.

## Key Features

1. **User Management**: Registration, authentication, and profile management for users.
2. **Pet Listings**: Ability to create, update, and delete pet adoption or lost pet listings.
3. **Image Handling**: Integration with AWS S3 for efficient storage and retrieval of pet images.
4. **Notification System**: RabbitMQ-based system to notify users about new pets in their area.
5. **Email Service**: Automated email notifications using Thymeleaf templates.
6. **Admin Panel**: Special routes and functionalities for admin users.
7. **API Security**: JWT-based authentication and role-based access control.

## Project Structure

The project follows a standard Spring Boot application structure:

- `controller`: REST API endpoints
- `service`: Business logic implementation
- `repository`: Data access layer
- `entity`: JPA entities representing database tables
- `dto`: Data Transfer Objects for API requests and responses
- `config`: Configuration classes for Spring Boot, Security, and other integrations
- `utils`: Utility classes and helper functions

## API Documentation
API documentation is available through Swagger UI.
