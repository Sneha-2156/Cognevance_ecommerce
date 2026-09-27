import { useState, useEffect } from 'react'
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts'
import { api } from '../api/client.js'

export default function AdminAnalytics() {
  const [summary, setSummary] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api.getDashboard().then(setSummary).catch((err) => setError(err.message))
  }, [])

  if (error) return <section className="section"><p className="form-status form-status--error">{error}</p></section>
  if (!summary) return <section className="section"><p>Loading dashboard...</p></section>

  return (
    <section className="section">
      <h2 className="section__title">Admin — Analytics</h2>

      <div className="stat-grid">
        <div className="stat-card">
          <p className="stat-card__label">Total Revenue</p>
          <p className="stat-card__value">₹{Number(summary.totalRevenue).toFixed(2)}</p>
        </div>
        <div className="stat-card">
          <p className="stat-card__label">Paid Orders</p>
          <p className="stat-card__value">{summary.totalOrders}</p>
        </div>
        <div className="stat-card">
          <p className="stat-card__label">Pending Orders</p>
          <p className="stat-card__value">{summary.pendingOrders}</p>
        </div>
      </div>

      <div className="chart-grid">
        <div className="chart-card">
          <h3>Monthly Revenue</h3>
          <ResponsiveContainer width="100%" height={260}>
            <LineChart data={summary.monthlyRevenue}>
              <CartesianGrid strokeDasharray="3 3" stroke="#262b3d" />
              <XAxis dataKey="month" stroke="#a3a8bd" />
              <YAxis stroke="#a3a8bd" />
              <Tooltip contentStyle={{ background: '#161925', border: '1px solid #262b3d' }} />
              <Line type="monotone" dataKey="revenue" stroke="#6c8cff" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="chart-card">
          <h3>Top Selling Products</h3>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={summary.topProducts}>
              <CartesianGrid strokeDasharray="3 3" stroke="#262b3d" />
              <XAxis dataKey="name" stroke="#a3a8bd" />
              <YAxis stroke="#a3a8bd" />
              <Tooltip contentStyle={{ background: '#161925', border: '1px solid #262b3d' }} />
              <Bar dataKey="unitsSold" fill="#7ee7c7" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </section>
  )
}
