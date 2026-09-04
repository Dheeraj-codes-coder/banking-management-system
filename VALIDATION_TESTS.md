# Validation tests

Run these tests in Postman after restarting the application.

## Invalid customer email

`POST http://localhost:8080/customer/save`

```json
{
  "name": "Test User",
  "email": "wrong-email",
  "phone": 9876543210,
  "address": "Kalaburagi"
}
```

Expected status: `400 Bad Request`

Expected response contains:

```json
{
  "email": "Enter a valid email address"
}
```

## Missing customer name

`POST http://localhost:8080/customer/save`

```json
{
  "name": "",
  "email": "test@gmail.com",
  "phone": 9876543210,
  "address": "Kalaburagi"
}
```

Expected status: `400 Bad Request`

## Negative opening balance

`POST http://localhost:8080/account/save/1`

```json
{
  "accountNumber": "SB10003",
  "accountType": "SAVINGS",
  "balance": -500
}
```

Expected status: `400 Bad Request`

## Invalid account type

`POST http://localhost:8080/account/save/1`

```json
{
  "accountNumber": "SB10003",
  "accountType": "STUDENT",
  "balance": 500
}
```

Expected response:

```text
Account type must be SAVINGS or CURRENT
```

## Insufficient balance

`PATCH http://localhost:8080/account/withdraw/1/999999`

Expected response:

```text
Insufficient balance
```
