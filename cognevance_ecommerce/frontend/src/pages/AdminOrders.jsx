import { useState, useEffect } from 'react'
import { api } from '../api/client.js'

const STATUSES = ['PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED']

export default function AdminOrders() {
  const [orders, setOrders] = useState([])
  const [message, setMessage] = useState('')

  const load = () => api.allOrders().then(setOrders).catch((err) => setMessage(err.message))

  useEffect(() => { load() }, [])

  const handleStatusChange = async (id, status) => {
    try {
      await api.updateOrderStatus(id, status)
      load()
    } catch (err) {
      setMessage(err.message)
    }
  }

  return (
    <section className="section">
      <h2 className="section__title">Admin — Orders</h2>
      {message && <p className="form-status">{message}</p>}

      <table className="admin-table">
        <thead><tr><th>Order</th><th>Customer</th><th>Total</th><th>Status</th><th>Date</th></tr></thead>
        <tbody>
          {orders.map((o) => (
            <tr key={o.id}>
              <td>#{o.id}</td>
              <td>{o.user?.name}</td>
              <td>₹{o.totalAmount}</td>
              <td>
                <select value={o.status} onChange={(e) => handleStatusChange(o.id, e.target.value)}>
                  {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                </select>
              </td>
              <td>{new Date(o.createdAt).toLocaleDateString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}
