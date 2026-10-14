# Duck Store - Warehouse Management and Order Pricing

A full-stack application for managing rubber-duck warehouse inventory and calculating order prices. Built with Java 21, Spring Boot, MySQL, and React.
<img width="1333" height="621" alt="image" src="https://github.com/user-attachments/assets/ab7c2739-03f5-4d43-a714-f164e728c84e" />
<img width="1342" height="594" alt="image" src="https://github.com/user-attachments/assets/c24a08ca-582b-4ad5-be93-354c3e8de322" />


## Features

### Warehouse
- Create, list, edit, and logically delete ducks.
- Display active inventory sorted by quantity in ascending order.
- Validate color, size, price, and quantity.
- Merge new quantities into an existing active duck when color, size, and price match.
- Use a unique active key and MySQL upsert to support atomic merging of concurrent additions.
- Allow edits to price and quantity while keeping color and size unchanged.
- Preserve deleted records in the database while excluding them from the active listing.

### Order pricing API
- Calculate order prices using warehouse inventory; the request does not contain a price.
- Select the lowest-priced active duck matching the requested color and size.
- Return the package type, protection materials, total price, and itemized price breakdown.
- Apply bulk discounts, packaging adjustments, destination surcharges, and shipping fees.

### Packaging rules
| Duck size | Package |
|---|---|
| XLarge, Large | Wood |
| Medium | Cardboard |
| Small, XSmall | Plastic |

Protection materials depend on the shipping mode:
- **Air:** polystyrene balls for wood/cardboard; bubble-wrap bags for plastic.
- **Land:** polystyrene balls for all package types.
- **Sea:** moisture-absorbing beads and bubble-wrap bags for all package types.

### Pricing rules
- Base cost: quantity × unit price.
- More than 100 units: 20% discount on base cost.
- Wood: 5% surcharge on base cost.
- Plastic: 10% surcharge on base cost.
- Cardboard: 1% discount on base cost.
- USA: 18% destination surcharge.
- Bolivia: 13% destination surcharge.
- India: 19% destination surcharge.
- Other destinations: 15% destination surcharge.
- Sea shipping: $400.
- Land shipping: $10 per unit.
- Air shipping: $30 per unit; orders exceeding 1,000 units receive a 15% discount on the air-shipping charge.

Monetary calculations use `BigDecimal` and round monetary amounts to two decimal places using `HALF_UP`.

## Technology stack

- Backend: Java 21, Spring Boot, Spring Data JPA, Hibernate, Maven
- Database: MySQL
- Frontend: React, Vite, JavaScript, CSS
- Testing: JUnit 5, Mockito

## Prerequisites

Install:
- JDK 21
- MySQL 8 or compatible version
- Node.js and npm

## Database setup

Create the database in MySQL:

```sql
CREATE DATABASE duck_store_db;
```

Check `src/main/resources/application.properties` and configure the datasource URL, username, and password for your local MySQL instance. Do not commit real database passwords or other secrets.

The default local database URL used by this project is:

```text
jdbc:mysql://localhost:3306/duck_store_db
```

Ensure that the configured Hibernate schema setting matches the project's database setup. Start MySQL before running the backend.

## Run the backend

From the project root:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

## Run the frontend

Open a separate terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open the local URL printed by Vite, usually:

```text
http://localhost:5173
```

## API endpoints

### Warehouse

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/ducks` | List active ducks |
| POST | `/api/ducks` | Add or merge a duck |
| PUT | `/api/ducks/{id}` | Update price and quantity |
| DELETE | `/api/ducks/{id}` | Logically delete a duck |

Example request to add a duck:

```json
{
  "color": "RED",
  "size": "XLARGE",
  "price": 25.00,
  "quantity": 10
}
```

### Order pricing

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/orders` | Calculate order packaging and price |

Example request:

```json
{
  "color": "RED",
  "size": "XLARGE",
  "quantity": 10,
  "country": "India",
  "shippingMode": "LAND"
}
```

The price is resolved from active warehouse inventory. When multiple active records match the requested color and size at different prices, the lowest-priced record is selected.

## Tests

Run the automated backend tests:

```powershell
.\mvnw.cmd test
```

The current test suite contains 9 tests covering application context startup, packaging rules, and order-pricing calculations, including bulk discounts, air-shipping discounts, and missing inventory.

## Design decisions

- **Inventory identity:** color, size, and price determine whether two active ducks represent the same inventory record.
- **Price normalization:** unit prices are normalized to two decimal places.
- **Concurrent additions:** a unique active-key constraint and MySQL upsert are used to merge matching quantities atomically.
- **Deletion:** deletion is logical so historical records remain in the database.
- **Order price selection:** the lowest-priced active record matching color and size is selected because an order request does not include a price.
- **Scope:** the frontend manages the warehouse; order pricing is exposed through a backend API.

