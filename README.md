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

***

## API Examples

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
```

#### **B. Product-Wise Coupon (20% off Product ID 501)**

```json
{
  "type": "product-wise",
  "details": {
    "product_id": 501,
    "discount": 20.00
  },
  "isActive": true
}
```

#### **C. BxGy Coupon (Buy 2 of Product 100, Get 1 of Product 200 Free, max 3 times)**

```json
{
  "type": "bxgy",
  "details": {
    "buy_products": [
      {
        "product_id": 100,
        "quantity": 2
      }
    ],
    "get_products": [
      {
        "product_id": 200,
        "quantity": 1
      }
    ],
    "repition_limit": 3
  },
  "isActive": true
}
```

### 2. Get Applicable Coupons (`POST /applicable-coupons`)

This request identifies which coupons the cart qualifies for and calculates the potential discount for each.

#### **Request Body (`CartWrapper` format)**

This cart has a raw total of $185.00.

```json
{
  "cart": {
    "items": [
      {
        "product_id": 501, 
        "quantity": 2, 
        "price": 50.00 
      },
      {
        "product_id": 100, 
        "quantity": 3, 
        "price": 25.00 
      },
      {
        "product_id": 200, 
        "quantity": 1, 
        "price": 10.00
      }
    ],
    "total_price": 185.00, 
    "total_discount": 0.00, 
    "final_price": 185.00
  }
}
```

#### **Sample Response**

(Assuming coupons with IDs 1, 2, 3 were created based on the first three examples)

```json
{
    "applicable_coupons": [
        {
            "discount": 18.50,
            "coupon_id": 1,
            "type": "cart-wise"
        },
        {
            "discount": 20.00,
            "coupon_id": 2,
            "type": "product-wise"
        },
        {
            "discount": 10.00,
            "coupon_id": 3,
            "type": "bxgy"
        }
    ]
}
```

### 3. Apply a Coupon (`POST /apply-coupon/{id}`)

This endpoint applies a specific coupon to the cart and returns the updated `Cart` object with discount fields populated.

#### **Endpoint**

`POST http://localhost:8080/apply-coupon/1` (Applying the Cart-Wise Coupon, $18.50 discount)

#### **Response Body**

```json
{
    "updated_cart": {
        "items": [
            {
                "quantity": 2,
                "price": 50.00,
                "product_id": 501,
                "total_discount": null 
            },
            {
                "quantity": 3,
                "price": 25.00,
                "product_id": 100,
                "total_discount": null
            },
            {
                "quantity": 1,
                "price": 10.00,
                "product_id": 200,
                "total_discount": null
            }
        ],
        "total_price": 185.00,      
        "total_discount": 18.50,    
        "final_price": 166.50       
    }
}
```

## ⚠️ Limitations & Assumptions

* **No Stacking Logic:** The `/apply-coupon/{id}` endpoint applies only one coupon at a time. It does not calculate the optimal combination or conflict resolution for multiple coupons.
* **Exclusive Application:** Applying a new coupon overwrites any previously set discounts on the cart or item level.
* **BxGy Limitations:** The BxGy strategy currently only supports a 100% discount ("free") for the "Get" items and does not handle tiered or progressive discounts within a single coupon.
* **Currency Precision:** All monetary calculations use `BigDecimal` with `RoundingMode.HALF_UP` to prevent floating-point arithmetic errors.
