# Wallet-API

A simple wallet management service that allows users to create wallets, check balances, deposit funds, withdraw funds, and transfer funds between users.

## Installation

### Prerequisites

- Java 21
- Maven or Gradle

### Clone the Repository

```sh
git clone https://github.com/JereRamirez/WalletAPI.git
cd <repository-folder>
```

### Build the Project

Using Gradle:

```sh
gradle build
```

Using Maven:

```sh
mvn clean package
```

## Running the Service

### Run with Gradle

```sh
gradle bootRun
```

### Run with Java

```sh
java -jar build/libs/wallet-service-0.0.1-SNAPSHOT.jar
```

### Running in Development Mode

H2 console is enabled in development mode. You can access it at:

```
http://localhost:8080/h2-console
```

### OpenAPI Support

The service supports OpenAPI. When running locally, you can access the API documentation at:

```
http://localhost:8080/swagger-ui/index.html
```

## API Endpoints

### Create a Wallet

```
POST /api/wallet/{userId}
```

- Creates a new wallet for the specified user.

### Get Wallet Balance

```
GET /api/wallet/{userId}/balance
```

- Retrieves the current balance of the user's wallet.

### Get Historical Balance

```
GET /api/wallet/{userId}/balance?timestamp={timestamp}
```

- Retrieves the balance of the user's wallet at a specific point in time.

### Deposit Funds

```
POST /api/wallet/{userId}/deposit?amount={amount}
```

- Deposits a specified amount into the user's wallet.

### Withdraw Funds

```
POST /api/wallet/{userId}/withdraw?amount={amount}
```

- Withdraws a specified amount from the user's wallet, ensuring sufficient balance.

### Transfer Funds

```
POST /api/wallet/{fromUserId}/transfer/{toUserId}?amount={amount}
```

- Transfers a specified amount from one user's wallet to another.

## Running Tests

### Run All Tests

Using Gradle:

```sh
gradle test
```

Using Maven:

```sh
mvn test
```

## Design Choices

### Functional Requirements

- **Create Wallet**: Implemented an endpoint to create wallets for users, ensuring each user can have only one wallet.
- **Retrieve Balance**: Exposed an endpoint to fetch the current balance of a user's wallet, ensuring real-time availability.
- **Retrieve Historical Balance**: Implemented a query mechanism to retrieve the balance of a wallet at any given historical timestamp using transaction logs.
- **Deposit Funds**: Provided an endpoint for users to deposit funds into their wallets, updating the balance accordingly.
- **Withdraw Funds**: Ensured users can withdraw funds while enforcing balance constraints.
- **Transfer Funds**: Implemented secure fund transfers between wallets, ensuring transactional integrity.

### Non-Functional Requirements

- **Mission-Critical Service**: Designed the service to be highly available, with minimal dependencies to ensure uptime.
- **Auditability**: Implemented a transaction history log to maintain a full record of all operations, allowing complete traceability for audits.
- **Logging**: Used `@Slf4j` for structured logging in key operations to facilitate monitoring and debugging.
- **Error Handling**: Implemented a `GlobalExceptionHandler` to handle scenarios like insufficient funds, duplicate wallet creation, and invalid requests gracefully.
- **Persistence**: Used JPA with an H2 in-memory database for development and testing, allowing seamless integration with a relational database.
- **Testing**: Developed comprehensive unit tests (`WalletServiceTest`) and integration tests (`WalletControllerIT`) to validate business logic and API correctness.

## Trade-Offs and Compromises

- **In-Memory Database**: Opted for H2 instead of a persistent database like PostgreSQL for development convenience, with plans to upgrade.
- **Lack of Authentication**: No authentication was implemented, assuming a trusted internal environment.
- **Simplified Transaction Handling**: No distributed transaction management or rollback mechanisms were incorporated.
- **Concurrency Handling**: Currently, the service does not implement optimistic or pessimistic locking, which may lead to race conditions in high-concurrency scenarios.

## Future Improvements

- Switch to a persistent database (e.g., PostgreSQL) with Liquibase for schema management.
- Add authentication and authorization mechanisms.
- Implement distributed tracing and monitoring.
- Introduce concurrency control mechanisms to handle simultaneous transactions safely.
- Enhance transaction history storage for more efficient auditing.
- Implement a caching layer to improve performance.
- Add support for multiple currencies and exchange rates.
- Implement a notification system for transaction alerts and updates.
- Introduce a rate-limiting mechanism to prevent abuse and DoS attacks.
- Implement a circuit breaker pattern to handle service failures gracefully.
- Introduce a containerization strategy using Docker and Kubernetes for scalability.
- Add support for additional API endpoints like user management, transaction history, and reporting.

#### Time Spent ~4 hours