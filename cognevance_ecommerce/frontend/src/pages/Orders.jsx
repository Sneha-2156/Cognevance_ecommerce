import { useState, useEffect } from 'react'
import { api } from '../api/client.js'

const STATUS_COLORS = {
  PENDING: 'status--pending',
  PAID: 'status--paid',
  SHIPPED: 'status--shipped',
  DELIVERED: 'status--delivered',
  CANCELLED: 'status--cancelled',
}

export default function Orders() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.myOrders().then(setOrders).catch((err) => setError(err.message)).finally(() => setLoading(false))
  }, [])

  return (
    <section className="section">
      <h2 className="section__title">My Orders</h2>
      {error && <p className="form-status form-status--error">{error}</p>}

      {loading ? <p>Loading...</p> : orders.length === 0 ? (
        <p>You haven't placed any orders yet.</p>
      ) : (
        <div className="order-list">
          {orders.map((o) => (
            <div className="order-card" key={o.id}>
              <div className="order-card__header">
                <h3>Order #{o.id}</h3>
                <span className={`status-badge ${STATUS_COLORS[o.status] || ''}`}>{o.status}</span>
              </div>
              <p className="order-card__date">{new Date(o.createdAt).toLocaleString()}</p>
              <ul className="order-card__items">
                {o.items.map((item) => (
                  <li key={item.id}>{item.product.name} x{item.quantity} — ₹{item.priceAtPurchase}</li>
                ))}
              </ul>
              <p className="order-card__total">Total: ₹{o.totalAmount}</p>
              <p className="order-card__address">Shipping to: {o.shippingAddress}</p>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}
