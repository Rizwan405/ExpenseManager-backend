# Expense Tracker Backend

A robust backend application for managing expenses, built with **Spring Boot**, **Liquibase**, and **JWT Authentication**. This application provides APIs for user registration, login, and transaction management, and integrates seamlessly with an Angular frontend. Data is stored in a **MySQL** database.

## Features
- **User Authentication**: Secure login and registration using JWT (JSON Web Tokens).
- **Transaction Management**: CRUD operations for transactions (insert, update, delete).
- **Spring Security**: Role-based access control and secure API endpoints.
- **Exception Handling**: Custom exceptions and global exception handlers for robust error management.
- **Database Migrations**: Liquibase for managing database schema changes.

## Technologies Used
- **Backend**: Spring Boot
- **Database**: MySQL
- **Database Migrations**: Liquibase
- **Authentication**: JWT (JSON Web Tokens)
- **Security**: Spring Security
- **Frontend Integration**: Angular (via REST APIs)
- **Build Tool**: Maven

## API Endpoints
### Authentication
- **Register User**:
  - `POST /api/auth/register`
  - Request Body:
    ```json
    {
      "username": "user123",
      "password": "password123"
    }
    ```

- **Login User**:
  - `POST /api/auth/login`
  - Request Body:
    ```json
    {
      "username": "user123",
      "password": "password123"
    }
    ```
  - Response:
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
    ```

### Transactions
- **Get All Transactions**:
  - `GET /api/transactions`
  - **Headers**: `Authorization: Bearer <token>`

- **Add a Transaction**:
  - `POST /api/transactions`
  - **Headers**: `Authorization: Bearer <token>`
  - Request Body:
    ```json
    {
      "amount": 100.0,
      "description": "Groceries",
      "date": "2023-10-01"
    }
    ```

- **Update a Transaction**:
  - `PUT /api/transactions/{id}`
  - **Headers**: `Authorization: Bearer <token>`
  - Request Body:
    ```json
    {
      "amount": 150.0,
      "description": "Updated Groceries",
      "date": "2023-10-01"
    }
    ```

- **Delete a Transaction**:
  - `DELETE /api/transactions/{id}`
  - **Headers**: `Authorization: Bearer <token>`

## Authentication
- **JWT Token Generation**:
  - When a user logs in, a JWT token is generated and returned in the response.
  - This token must be included in the `Authorization` header for all subsequent requests.

- **Token Validation**:
  - Every request is validated for a valid JWT token.
  - If the token is invalid or expired, the request is rejected.

## Exception Handling
- **Custom Exceptions**:
  - Custom exceptions are thrown for specific scenarios (e.g., invalid credentials, transaction not found,Resource not found ).
- **Global Exception Handler**:
  - A global exception handler catches all exceptions and returns a standardized error response.

## Database Schema
The database consists of the following tables:
- **Users**:
  - `id` (Primary Key)
  - `username` (Unique)
  - `password` (Encrypted)
- **Transactions**:
  - `id` (Primary Key)
  - `amount`
  - `description`
  - `date`
  - `payment type`
  - `Category`
  - `payment method`
  - `user_id` (Foreign Key to Users)

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
