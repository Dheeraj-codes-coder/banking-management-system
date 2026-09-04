# Banking Management System

A Spring Boot and MySQL REST API project for managing customers, accounts and banking transactions.

The project includes a responsive HTML, CSS and JavaScript frontend served directly by Spring Boot.

## Free cloud deployment

The project is ready to deploy as a Docker container. Follow every step in
[`DEPLOYMENT_FREE.md`](DEPLOYMENT_FREE.md) to upload it to GitHub, create a
free MySQL-compatible TiDB Cloud database, and deploy the application on a
free Koyeb web service.

## Technologies

- Java 17 or later
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Security
- BCrypt password encryption
- JUnit and Mockito
- HTML, CSS and JavaScript
- MySQL
- Maven
- Postman

## Eclipse setup

1. Open Eclipse.
2. Select `File > Import > Existing Maven Projects`.
3. Select the `banking-app` folder.
4. Open `src/main/resources/application.properties`.
5. Change `spring.datasource.password=root` to your MySQL password.
6. Right-click the project and select `Maven > Update Project`.
7. Run `BankingAppApplication.java` as a Java Application or Spring Boot App.

8. Open `http://localhost:8080` in a browser.

The database named `banking_app_project` is created automatically when the MySQL user has permission.

## Postman request order

### 1. Save first customer

- Method: `POST`
- URL: `http://localhost:8080/customer/save`
- Body: raw JSON

```json
{
  "name": "Dheeraj Patil",
  "email": "dheeraj@gmail.com",
  "phone": 9876543210,
  "address": "Kalaburagi",
  "password": "Dheeraj@123"
}
```

### 2. Save second customer

- Method: `POST`
- URL: `http://localhost:8080/customer/save`

```json
{
  "name": "Rahul Kumar",
  "email": "rahul@gmail.com",
  "phone": 9876500000,
  "address": "Bengaluru",
  "password": "Rahul@123"
}
```

### 3. Create accounts

- Method: `POST`
- URL: `http://localhost:8080/account/save/1`

```json
{
  "accountNumber": "SB10001",
  "accountType": "SAVINGS",
  "balance": 5000
}
```

Create another account with URL `http://localhost:8080/account/save/2` and account number `SB10002`.

### 4. Deposit

- Method: `PATCH`
- URL: `http://localhost:8080/account/deposit/1/1000`

### 5. Withdraw

- Method: `PATCH`
- URL: `http://localhost:8080/account/withdraw/1/500`

### 6. Transfer

- Method: `PATCH`
- URL: `http://localhost:8080/account/transfer/1/2/1000`

### 7. Check transaction history

- Method: `GET`
- URL: `http://localhost:8080/transaction/account/1`

## Other endpoints

| Method | URL | Purpose |
| --- | --- | --- |
| GET | `/customer/fetch` | Fetch all customers |
| GET | `/customer/find/{customerId}` | Find customer |
| PATCH | `/customer/update/{customerId}` | Update customer |
| DELETE | `/customer/delete/{customerId}` | Delete customer |
| GET | `/account/fetch` | Fetch all accounts |
| GET | `/account/find/{accountId}` | Find account |
| GET | `/account/customer/{customerId}` | Customer accounts |
| GET | `/account/balance/{accountId}` | Check balance |
| GET | `/transaction/fetch` | Fetch all transactions |

## Current milestone

The core banking flow, input validation, central error handling, database authentication, role authorization, automated tests and OpenAPI documentation are complete.

## Security

Customer registration is public. Every other API requires HTTP Basic authentication.

Default admin credentials for local demonstration:

```text
Username: admin@bank.com
Password: Admin@123
```

For customer registration, include a password:

```json
{
  "name": "Dheeraj Patil",
  "email": "dheeraj.new@gmail.com",
  "phone": 9876543210,
  "address": "Kalaburagi",
  "password": "Dheeraj@123"
}
```

Test customer login using `GET http://localhost:8080/login`. In Postman, select `Authorization > Basic Auth` and enter the registered email and password.

The password is stored as a BCrypt hash and is never returned in customer JSON responses. Change the default admin credentials through `BANK_ADMIN_EMAIL` and `BANK_ADMIN_PASSWORD` before deployment, and use HTTPS outside local development.

## API documentation

After starting the project, open:

```text
http://localhost:8080/openapi.yaml
```

## Frontend pages

| Page | URL | Purpose |
| --- | --- | --- |
| Login | `http://localhost:8080/` | Customer and administrator login |
| Registration | `http://localhost:8080/register.html` | Create a customer profile |
| Customer dashboard | `http://localhost:8080/dashboard.html` | Accounts, balances and transactions |
| Administrator dashboard | `http://localhost:8080/admin.html` | System-wide overview |
| Customers | `http://localhost:8080/customers.html` | Customer profiles and deletion |
| Accounts | `http://localhost:8080/accounts.html` | All accounts and balances |
| Transactions | `http://localhost:8080/transactions.html` | Complete transaction ledger |

The customer dashboard only uses ownership-checked endpoints. A customer cannot deposit into, withdraw from or view another customer's account by changing an ID in the browser.

## Automated tests

In Eclipse, right-click the project and choose `Run As > Maven test`. The project contains controller tests for deposits, insufficient balances, customer registration and account-ownership protection.
