# MamaCare Backend API Documentation

## Base Information

**Base URL:** `http://localhost:8080/api/v1` (development)  
**Server:** Spring Boot REST API  
**Database:** MySQL  
**Authentication:** JWT Token-based  
**CORS Allowed Origins:** `http://localhost:19006`

---

## Authentication & Authorization

All endpoints (except `/auth/register` and `/auth/authenticate`) require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer <your_jwt_token>
```

JWT Token Details:

- **Expiration:** 24 hours (86400000 ms)
- **Refresh Token Expiration:** 7 days (604800000 ms)

---

## API Endpoints

### 1. Authentication Endpoints

**Base Path:** `/api/v1/auth`

#### Register User

- **Method:** `POST`
- **Endpoint:** `/register`
- **Description:** Register a new user account
- **Authentication:** Not required
- **Request Body:**
  ```json
  {
    "email": "user@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }
  ```
  _(Refer to RegisterRequestDto)_
- **Response:** `AuthenticationResponse`
  ```json
  {
    "accessToken": "jwt_token_here",
    "refreshToken": "refresh_token_here"
  }
  ```
- **Status Codes:** 200 OK

#### Authenticate User (Login)

- **Method:** `POST`
- **Endpoint:** `/authenticate`
- **Description:** Login with email and password
- **Authentication:** Not required
- **Request Body:** `AuthenticationRequest`
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **Response:** `AuthenticationResponse`
  ```json
  {
    "accessToken": "jwt_token_here",
    "refreshToken": "refresh_token_here"
  }
  ```
- **Status Codes:** 200 OK

#### Refresh Token

- **Method:** `POST`
- **Endpoint:** `/refresh-token`
- **Description:** Refresh your access token using the refresh token
- **Authentication:** Not required (uses refresh token from request)
- **Request:** Include refresh token in Authorization header or cookies
- **Response:** New access and refresh tokens
- **Status Codes:** 200 OK

---

### 2. User Management Endpoints

**Base Path:** `/api/v1/users`

#### Change Password

- **Method:** `PATCH`
- **Endpoint:** `/`
- **Description:** Change the current user's password
- **Authentication:** Required (JWT Token)
- **Request Body:** `ChangePasswordRequest`
  ```json
  {
    "currentPassword": "old_password",
    "newPassword": "new_password",
    "confirmationPassword": "new_password"
  }
  ```
- **Response:** 200 OK (no body)
- **Status Codes:** 200 OK

---

### 3. Pregnancy Management Endpoints

**Base Path:** `/api/v1/pregnancy`

#### Setup Pregnancy Info

- **Method:** `POST`
- **Endpoint:** `/setup`
- **Description:** Set up pregnancy information (LMP, due date, etc.)
- **Authentication:** Required (JWT Token)
- **Request Body:** `PregnancySetupRequest`
  ```json
  {
    "lastMenstrualPeriodDate": "2024-01-15",
    "dueDate": "2024-10-22",
    "weeksOfPregnancy": 8
  }
  ```
  _(Refer to PregnancySetupRequest DTO)_
- **Response:** `PregnancySummaryResponse`
  ```json
  {
    "weeksOfPregnancy": 8,
    "daysOfPregnancy": 56,
    "dueDate": "2024-10-22",
    "lastMenstrualPeriodDate": "2024-01-15"
  }
  ```
- **Status Codes:** 200 OK

#### Get Current User's Pregnancy Info

- **Method:** `GET`
- **Endpoint:** `/me`
- **Description:** Retrieve the current user's pregnancy information
- **Authentication:** Required (JWT Token)
- **Response:** `PregnancySummaryResponse`
  ```json
  {
    "weeksOfPregnancy": 8,
    "daysOfPregnancy": 56,
    "dueDate": "2024-10-22",
    "lastMenstrualPeriodDate": "2024-01-15"
  }
  ```
- **Status Codes:** 200 OK

---

### 4. Home/Dashboard Endpoints

**Base Path:** `/api/v1/home`

#### Get Home Summary

- **Method:** `GET`
- **Endpoint:** `/summary`
- **Description:** Get dashboard summary data (medications, appointments, fitness, etc.)
- **Authentication:** Required (JWT Token)
- **Response:** `HomeSummaryResponse`
  _(Contains aggregated data from multiple features)_
- **Status Codes:** 200 OK

---

### 5. Appointment Endpoints

**Base Path:** `/api/v1/appointments`

#### Create Appointment

- **Method:** `POST`
- **Endpoint:** `/`
- **Description:** Create a new appointment
- **Authentication:** Required (JWT Token)
- **Request Body:** `CreateAppointmentRequest`
  ```json
  {
    "title": "Doctor's Checkup",
    "description": "Regular prenatal checkup",
    "appointmentDate": "2024-06-15T10:00:00",
    "appointmentType": "CHECKUP",
    "reminderOffsets": ["PT_5_MINUTES", "PT_1_DAY"],
    "checklistItems": [
      {
        "itemName": "Bring insurance card",
        "isCompleted": false
      }
    ]
  }
  ```
  _(Refer to CreateAppointmentRequest DTO)_
- **Response:** `AppointmentResponse`
  ```json
  {
    "id": 1,
    "title": "Doctor's Checkup",
    "description": "Regular prenatal checkup",
    "appointmentDate": "2024-06-15T10:00:00",
    "appointmentType": "CHECKUP",
    "status": "SCHEDULED",
    "reminders": [],
    "checklistItems": []
  }
  ```
- **Status Codes:** 201 Created

#### Get Next Upcoming Appointment

- **Method:** `GET`
- **Endpoint:** `/upcoming/next`
- **Description:** Get the next upcoming appointment for the current user
- **Authentication:** Required (JWT Token)
- **Response:** `NextAppointmentResponse`
  ```json
  {
    "id": 1,
    "title": "Doctor's Checkup",
    "description": "Regular prenatal checkup",
    "appointmentDate": "2024-06-15T10:00:00",
    "appointmentType": "CHECKUP",
    "daysUntilAppointment": 6,
    "hoursUntilAppointment": 150
  }
  ```
- **Status Codes:** 200 OK

#### Update Checklist Item

- **Method:** `PATCH`
- **Endpoint:** `/{appointmentId}/checklist/{itemId}`
- **Description:** Update a checklist item's status (mark as completed/incomplete)
- **Authentication:** Required (JWT Token)
- **Path Parameters:**
  - `appointmentId` (Long): The appointment ID
  - `itemId` (Long): The checklist item ID
- **Request Body:** `UpdateChecklistItemRequest`
  ```json
  {
    "isCompleted": true
  }
  ```
- **Response:** `AppointmentChecklistItemResponse`
  ```json
  {
    "id": 1,
    "itemName": "Bring insurance card",
    "isCompleted": true
  }
  ```
- **Status Codes:** 200 OK

**Appointment Types:** `CHECKUP`, `ULTRASOUND`, `BLOOD_TEST`, `DELIVERY`, `FOLLOW_UP`  
**Appointment Status:** `SCHEDULED`, `COMPLETED`, `CANCELLED`, `RESCHEDULED`  
**Reminder Offsets:** `PT_5_MINUTES`, `PT_15_MINUTES`, `PT_30_MINUTES`, `PT_1_HOUR`, `PT_1_DAY`, `PT_3_DAYS`

---

### 6. Medication Reminder Endpoints

**Base Path:** `/api/v1/medications`

#### Create Medication

- **Method:** `POST`
- **Endpoint:** `/`
- **Description:** Create a new medication reminder
- **Authentication:** Required (JWT Token)
- **Request Body:** `CreateMedicationRequest`
  ```json
  {
    "medicationName": "Prenatal Vitamins",
    "dosage": "1 tablet",
    "frequency": "DAILY",
    "dosageUnit": "tablet",
    "quantity": 30,
    "prescribedDate": "2024-01-01",
    "reminderTime": "08:00:00",
    "notes": "Take with food"
  }
  ```
  _(Refer to CreateMedicationRequest DTO)_
- **Response:** `MedicationResponse`
  ```json
  {
    "id": 1,
    "medicationName": "Prenatal Vitamins",
    "dosage": "1 tablet",
    "frequency": "DAILY",
    "dosageUnit": "tablet",
    "quantity": 30,
    "prescribedDate": "2024-01-01",
    "reminderTime": "08:00:00",
    "notes": "Take with food",
    "isActive": true
  }
  ```
- **Status Codes:** 201 Created

#### Get Today's Medications

- **Method:** `GET`
- **Endpoint:** `/today`
- **Description:** Get all medications due today
- **Authentication:** Required (JWT Token)
- **Response:** `MedicationHomeResponse`
  ```json
  {
    "medicationsDueToday": [
      {
        "id": 1,
        "medicationName": "Prenatal Vitamins",
        "dosage": "1 tablet",
        "reminderTime": "08:00:00",
        "isTaken": false
      }
    ],
    "totalMedicationsToday": 1,
    "medicationsTakenToday": 0
  }
  ```
- **Status Codes:** 200 OK

#### Get All Medications

- **Method:** `GET`
- **Endpoint:** `/`
- **Description:** Get all medications for the current user
- **Authentication:** Required (JWT Token)
- **Response:** List of `MedicationResponse`
  ```json
  [
    {
      "id": 1,
      "medicationName": "Prenatal Vitamins",
      "dosage": "1 tablet",
      "frequency": "DAILY",
      "dosageUnit": "tablet",
      "quantity": 30,
      "prescribedDate": "2024-01-01",
      "reminderTime": "08:00:00",
      "notes": "Take with food",
      "isActive": true
    }
  ]
  ```
- **Status Codes:** 200 OK

#### Mark Medication as Taken

- **Method:** `PATCH`
- **Endpoint:** `/{medicationId}/taken`
- **Description:** Mark a medication as taken for today
- **Authentication:** Required (JWT Token)
- **Path Parameters:**
  - `medicationId` (Long): The medication ID
- **Request Body:** None
- **Response:** `MarkMedicationTakenResponse`
  ```json
  {
    "medicationId": 1,
    "medicationName": "Prenatal Vitamins",
    "isTaken": true,
    "takenTime": "2024-06-09T08:30:00"
  }
  ```
- **Status Codes:** 200 OK

#### Deactivate Medication

- **Method:** `PATCH`
- **Endpoint:** `/{medicationId}/inactive`
- **Description:** Deactivate a medication (no longer needed)
- **Authentication:** Required (JWT Token)
- **Path Parameters:**
  - `medicationId` (Long): The medication ID
- **Request Body:** None
- **Response:** No content
- **Status Codes:** 204 No Content

**Medication Frequencies:** `DAILY`, `TWICE_DAILY`, `THREE_TIMES_DAILY`, `FOUR_TIMES_DAILY`, `WEEKLY`, `BIWEEKLY`, `MONTHLY`

---

### 7. Walk/Fitness Exercise Endpoints

**Base Path:** `/api/v1/walk-exercise`

#### Get Home/Dashboard

- **Method:** `GET`
- **Endpoint:** `/home`
- **Description:** Get fitness dashboard data (stats, current session, goals, etc.)
- **Authentication:** Required (JWT Token)
- **Response:** `WalkHomeResponse`
  ```json
  {
    "totalSteps": 25000,
    "totalDistance": 18.5,
    "caloriesBurned": 450,
    "todaysSteps": 5000,
    "weeklyGoal": 50000,
    "weeklyProgress": 50.0,
    "currentSession": null,
    "recentSessions": []
  }
  ```
- **Status Codes:** 200 OK

#### Start Walk Session

- **Method:** `POST`
- **Endpoint:** `/sessions`
- **Description:** Start a new walk exercise session
- **Authentication:** Required (JWT Token)
- **Request Body:** `StartWalkSessionRequest` (optional)
  ```json
  {
    "goalSteps": 5000,
    "goalDuration": 30
  }
  ```
  _(Can be empty/null)_
- **Response:** `WalkSessionResponse`
  ```json
  {
    "id": 1,
    "startTime": "2024-06-09T10:00:00",
    "endTime": null,
    "duration": 0,
    "steps": 0,
    "distance": 0.0,
    "calories": 0.0,
    "goalSteps": 5000,
    "goalDuration": 30,
    "status": "IN_PROGRESS"
  }
  ```
- **Status Codes:** 201 Created

#### Sync Walk Session Metrics

- **Method:** `PATCH`
- **Endpoint:** `/sessions/{sessionId}/metrics`
- **Description:** Update metrics during an active walk session (steps, distance, etc.)
- **Authentication:** Required (JWT Token)
- **Path Parameters:**
  - `sessionId` (Long): The session ID
- **Request Body:** `SyncWalkSessionMetricsRequest`
  ```json
  {
    "steps": 1200,
    "distance": 0.9,
    "calories": 85,
    "duration": 10
  }
  ```
- **Response:** `WalkSessionResponse`
  ```json
  {
    "id": 1,
    "startTime": "2024-06-09T10:00:00",
    "endTime": null,
    "duration": 10,
    "steps": 1200,
    "distance": 0.9,
    "calories": 85.0,
    "goalSteps": 5000,
    "goalDuration": 30,
    "status": "IN_PROGRESS"
  }
  ```
- **Status Codes:** 200 OK

#### Complete Walk Session

- **Method:** `PATCH`
- **Endpoint:** `/sessions/{sessionId}/complete`
- **Description:** Complete a walk session and record final metrics
- **Authentication:** Required (JWT Token)
- **Path Parameters:**
  - `sessionId` (Long): The session ID
- **Request Body:** `SyncWalkSessionMetricsRequest` (optional - final metrics)
  ```json
  {
    "steps": 5500,
    "distance": 4.1,
    "calories": 450,
    "duration": 35
  }
  ```
- **Response:** `WalkSessionResponse`
  ```json
  {
    "id": 1,
    "startTime": "2024-06-09T10:00:00",
    "endTime": "2024-06-09T10:35:00",
    "duration": 35,
    "steps": 5500,
    "distance": 4.1,
    "calories": 450.0,
    "goalSteps": 5000,
    "goalDuration": 30,
    "status": "COMPLETED"
  }
  ```
- **Status Codes:** 200 OK

**Session Status:** `IN_PROGRESS`, `COMPLETED`, `PAUSED`, `CANCELLED`

---

### 8. Management Endpoints

**Base Path:** `/api/v1/management`

#### Management GET

- **Method:** `GET`
- **Endpoint:** `/`
- **Description:** Demo management endpoint
- **Authentication:** Not restricted
- **Response:** `"GET:: management controller"`
- **Status Codes:** 200 OK

#### Management POST

- **Method:** `POST`
- **Endpoint:** `/`
- **Description:** Demo management endpoint
- **Authentication:** Not restricted
- **Response:** `"POST:: management controller"`
- **Status Codes:** 200 OK

#### Management PUT

- **Method:** `PUT`
- **Endpoint:** `/`
- **Description:** Demo management endpoint
- **Authentication:** Not restricted
- **Response:** `"PUT:: management controller"`
- **Status Codes:** 200 OK

#### Management DELETE

- **Method:** `DELETE`
- **Endpoint:** `/`
- **Description:** Demo management endpoint
- **Authentication:** Not restricted
- **Response:** `"DELETE:: management controller"`
- **Status Codes:** 200 OK

---

### 9. Admin Endpoints

**Base Path:** `/api/v1/admin`

> **Note:** These endpoints require `ROLE_ADMIN` and specific authorities. Hidden from Swagger documentation.

#### Admin GET

- **Method:** `GET`
- **Endpoint:** `/`
- **Authority Required:** `admin:read`
- **Response:** `"GET:: admin controller"`

#### Admin POST

- **Method:** `POST`
- **Endpoint:** `/`
- **Authority Required:** `admin:create`
- **Response:** `"POST:: admin controller"`

#### Admin PUT

- **Method:** `PUT`
- **Endpoint:** `/`
- **Authority Required:** `admin:update`
- **Response:** `"PUT:: admin controller"`

#### Admin DELETE

- **Method:** `DELETE`
- **Endpoint:** `/`
- **Authority Required:** `admin:delete`
- **Response:** `"DELETE:: admin controller"`

---

## Error Handling

The API uses standard HTTP status codes:

| Status Code | Meaning                                                    |
| ----------- | ---------------------------------------------------------- |
| 200         | OK - Request successful                                    |
| 201         | Created - Resource created successfully                    |
| 204         | No Content - Successful operation with no response body    |
| 400         | Bad Request - Invalid request parameters                   |
| 401         | Unauthorized - Missing or invalid JWT token                |
| 403         | Forbidden - Insufficient permissions or invalid token      |
| 404         | Not Found - Resource not found                             |
| 409         | Conflict - Resource already exists or constraint violation |
| 500         | Internal Server Error - Server-side error                  |

**Error Response Format:**

```json
{
  "timestamp": "2024-06-09T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": "Invalid request parameters"
}
```

---

## Security Headers

Include the following when making requests:

```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

---

## Common Request/Response DTOs

### Authentication DTOs

- **AuthenticationResponse:** Contains `accessToken` and `refreshToken`
- **AuthenticationRequest:** Contains `email` and `password`
- **RegisterRequestDto:** Contains user registration details

### User DTOs

- **ChangePasswordRequest:** Contains `currentPassword`, `newPassword`, `confirmationPassword`

### Response Wrappers

Most endpoints return either:

- Single object response
- List of objects
- HTTP status with no body (204 No Content)

---

## Example Frontend Requests

### Register

```javascript
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Login

```javascript
POST /api/v1/auth/authenticate
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

### Create Appointment (Authenticated)

```javascript
POST /api/v1/appointments
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "title": "Doctor's Checkup",
  "description": "Regular prenatal checkup",
  "appointmentDate": "2024-06-15T10:00:00",
  "appointmentType": "CHECKUP",
  "reminderOffsets": ["PT_5_MINUTES", "PT_1_DAY"],
  "checklistItems": [
    {
      "itemName": "Bring insurance card",
      "isCompleted": false
    }
  ]
}
```

### Get Today's Medications (Authenticated)

```javascript
GET /api/v1/medications/today
Authorization: Bearer <jwt_token>
```

### Start Walk Session (Authenticated)

```javascript
POST /api/v1/walk-exercise/sessions
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "goalSteps": 5000,
  "goalDuration": 30
}
```

---

## Rate Limiting

Currently, no rate limiting is enforced. This may be added in future updates.

---

## CORS Configuration

The API is configured to accept requests from:

- `http://localhost:19006` (React Native development)

For production, update the `CORS_ALLOWED_ORIGINS` in `application.yaml`.

---

## Database

**Type:** MySQL  
**Host:** 34.60.140.34  
**Port:** 3306  
**Database:** mamacare-database-real

The database automatically creates/updates tables on application startup (using Hibernate DDL auto: update).

---

## Support & Troubleshooting

1. **Invalid Token:** Ensure the JWT token is included in the Authorization header
2. **Token Expired:** Use the `/api/v1/auth/refresh-token` endpoint to get a new token
3. **403 Forbidden:** Check that your role/authorities have the required permissions
4. **404 Not Found:** Verify the endpoint path and resource ID are correct

---

**Last Updated:** June 9, 2024  
**API Version:** v1
