# Cognevance Project 3 — Enterprise E-Commerce & Analytics Platform

A scalable e-commerce platform: JWT auth with role-based access (customer/admin),
product & order management, Stripe payments, email order confirmations, product
image uploads, and an admin analytics dashboard with charts.

## Tech Stack
- **Frontend:** React 18 (Vite), React Router, Stripe.js/React Stripe.js, Recharts
- **Backend:** Java 17, Spring Boot 3 (Web, Security, Data JPA, Validation, Mail), JWT (jjwt), Stripe Java SDK
- **Database:** MySQL
- **Deployment targets:** Vercel/Netlify (frontend), Render/Railway (backend + MySQL)

## Architecture Overview
```
Customer                     Admin
   |                            |
   v                            v
React SPA  <---- JWT ---->  Spring Boot REST API
   |                            |
   | Stripe.js                  | Stripe Java SDK, JavaMailSender
   v                            v
 Stripe                    MySQL, local/cloud file storage
```
- **Auth:** stateless JWT (`JwtAuthFilter` + `SecurityConfig`), roles `CUSTOMER`/`ADMIN`.
- **Checkout flow:** cart (client-side, `localStorage`) → `POST /api/orders/checkout`
  creates an `Order` + reserves stock + creates a Stripe `PaymentIntent` → frontend
  confirms the card with Stripe.js → `POST /api/payments/confirm` marks the order `PAID`
  and (via the admin status endpoint) triggers an email confirmation.
- **Images:** `FileStorageService` saves to local disk and serves via `/uploads/**`;
  swap its `store()` method for an S3/Cloudinary SDK call to move to real cloud storage
  without touching any other layer.
- **Analytics:** `AnalyticsService` aggregates paid-order revenue, monthly revenue and
  top-selling products directly via JPQL queries, exposed at `/api/analytics/dashboard`.

## Project Structure
```
cognevance_ecommerce/
├── frontend/src/
│   ├── api/client.js         # fetch wrapper (JWT + multipart upload)
│   ├── context/               # AuthContext, CartContext
│   ├── components/            # Navbar, ProtectedRoute
│   └── pages/                 # Products, ProductDetail, Cart, Checkout, Orders,
│                               # Admin (+ AdminProducts, AdminOrders, AdminAnalytics)
└── backend/src/main/java/com/cognevance/ecommerce/
    ├── entity/        (User, Category, Product, Order, OrderItem, Payment)
    ├── repository/    (Spring Data JPA + analytics queries)
    ├── security/      (JwtUtil, JwtAuthFilter, AppUserDetailsService)
    ├── service/       (Auth, Product, Category, Order, Payment, Email, FileStorage, Analytics)
    ├── controller/    (Auth, Product, Category, Order, Payment, Upload, Analytics, Health)
    ├── dto/, config/, exception/
```

## Backend Setup
1. Install Java 17+, have MySQL running.
2. In `application.properties`, set DB credentials, a real `jwt.secret`, your
   Stripe **test** secret key, and SMTP creds for email (or export the matching
   env vars — `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `STRIPE_SECRET_KEY`,
   `MAIL_USERNAME`, `MAIL_PASSWORD`). Get Stripe test keys from
   https://dashboard.stripe.com/test/apikeys.
3. Run:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
4. To get an admin account: register normally, then set that user's `role` to
   `ADMIN` in MySQL (or pass `"role": "ADMIN"` in the register request body for
   local testing only — remove that option before a real deployment).

## Frontend Setup
```bash
cd frontend
npm install
```
Create `frontend/.env`:
```
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_your_key_here
```
Then:
```bash
npm run dev
```
Opens on `http://localhost:5173`; `/api` and `/uploads` are proxied to
`localhost:8080` in dev. Use Stripe's test card `4242 4242 4242 4242` (any
future expiry, any CVC) to complete a checkout.

## API Endpoints
| Method | Endpoint                         | Access        | Description                       |
|--------|------------------------------------|---------------|-------------------------------------|
| POST   | `/api/auth/register` / `/login`   | Public        | Auth, returns JWT                   |
| GET    | `/api/products`                   | Public        | List/search/filter products         |
| POST/PUT/DELETE | `/api/products/**`       | Admin         | Manage products                     |
| GET    | `/api/categories`                 | Public        | List categories                     |
| POST/DELETE | `/api/categories/**`         | Admin         | Manage categories                   |
| POST   | `/api/upload/image`               | Admin         | Upload a product image              |
| POST   | `/api/orders/checkout`            | Customer      | Create order + Stripe PaymentIntent |
| GET    | `/api/orders/me`                  | Customer      | My order history                    |
| GET    | `/api/orders`                     | Admin         | All orders                          |
| PUT    | `/api/orders/{id}/status`         | Admin         | Update order status                 |
| POST   | `/api/payments/confirm`           | Customer      | Confirm payment succeeded           |
| GET    | `/api/analytics/dashboard`        | Admin         | Revenue, orders, top products       |

## Deployment
- **Frontend:** deploy `frontend/`; set `VITE_API_BASE` (backend `/api` URL) and
  `VITE_STRIPE_PUBLISHABLE_KEY` as environment variables.
- **Backend:** deploy `backend/` on Render/Railway with a MySQL add-on; set
  `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `STRIPE_SECRET_KEY`,
  `MAIL_USERNAME`, `MAIL_PASSWORD`, and `UPLOAD_DIR` (point this at a persistent
  disk, or switch `FileStorageService` to S3/Cloudinary for real cloud storage).
- Restrict CORS in `SecurityConfig.java` from `*` to your real frontend domain.
- For production, prefer a Stripe **webhook** (`payment_intent.succeeded`) over
  the `/api/payments/confirm` endpoint used here, since it doesn't depend on the
  customer's browser staying online to report success.
