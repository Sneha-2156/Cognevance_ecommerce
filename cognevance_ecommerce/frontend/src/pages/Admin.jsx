import { Link, Outlet, useLocation } from 'react-router-dom'

const TABS = [
  { path: '/admin/analytics', label: 'Analytics' },
  { path: '/admin/products', label: 'Products' },
  { path: '/admin/orders', label: 'Orders' },
]

export default function Admin() {
  const location = useLocation()

  return (
    <div>
      <div className="admin-tabs">
        {TABS.map((t) => (
          <Link key={t.path} to={t.path}
            className={`admin-tab ${location.pathname === t.path ? 'is-active' : ''}`}>
            {t.label}
          </Link>
        ))}
      </div>
      <Outlet />
    </div>
  )
}
