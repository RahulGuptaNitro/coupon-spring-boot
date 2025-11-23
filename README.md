# coupon-spring-boot

A RESTful API for managing and applying dynamic discount coupons (Cart-wise, Product-wise, BxGy, and Amount-Off) for an e-commerce platform. Built with Java Spring Boot using the Strategy Design Pattern for maximum extensibility.

***

## 🚀 Getting Started

### Prerequisites

* Java 17 or higher
* Maven (or Gradle)

### Installation & Running

1.  **Build the project:**
    ```bash
    mvn clean install
    ```
2.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```

The API will be available at **`http://localhost:8080`**.

***

## 🧠 Design Decisions & Architecture

### The Strategy Pattern

To meet the requirement of easily adding new types of coupons in the future, the **Strategy Design Pattern** was implemented.

* **`CouponStrategy` Interface:** Defines the contract for all coupon logic, including methods like `supports`, `isApplicable`, `calculateDiscount`, and `apply`.
* **Concrete Strategies:** Each coupon type (e.g., `CartWiseStrategy`, `BxGyStrategy`) implements this interface.
* **Benefit:** Adding a new coupon type requires only creating a new class that implements `CouponStrategy`, without modifying the core service logic (`CouponServiceImpl.java`).

### Flexible Database Schema

The `details` field in the `Coupon` entity is a `Map<String, Object>`. This is mapped to a native **JSON** column type using **`@JdbcTypeCode(SqlTypes.JSON)`** to store unstructured configuration data. This allows different coupon types (like Cart-wise vs. BxGy) to store completely different configurations (e.g., a simple threshold vs. complex product arrays) without requiring database schema migrations.

***

## ✅ Implemented Coupon Strategies and Logic

The application supports four different types of coupon logic, each encapsulated in its own strategy class:

| Type Key | Strategy Class | Logic Summary |
| :--- | :--- | :--- |
| **`cart-wise`** | `CartWiseStrategy` | Applies a **percentage discount** to the entire cart if the total amount strictly exceeds a specific threshold. |
| **`product-wise`** | `ProductWiseStrategy` | Applies a **percentage discount** to the total cost of a **targeted product ID** if it exists in the cart. |
| **`bxgy`** | `BxGyStrategy` | Implements "Buy X, Get Y Free" logic, treating "Buy" items as a pool. Discounts the **cheapest** eligible "Get" items first. Supports a `repition_limit` to cap the deal. |
| **`amount-off`** | `AmountOffStrategy` | Applies a **fixed monetary value** discount if the cart total meets a minimum spend threshold. |

***

## 🧪 API Examples

### 1. Create Coupons (`POST /coupons`)

Below are examples of request bodies to create each type of coupon.

#### **A. Cart-Wise Coupon (10% off over $100)**

```json
{
  "type": "cart-wise",
  "details": {
    "threshold": 100.00,
    "discount": 10.00
  },
  "isActive": true
}
