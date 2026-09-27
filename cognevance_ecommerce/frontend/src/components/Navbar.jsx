import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth()
  const { count } = useCart()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className="navbar">
      <div className="navbar__inner">
        <Link to="/" className="navbar__brand">ShopSphere</Link>

        <nav className="navbar__links">
          <Link to="/products">Products</Link>
          {user && !isAdmin && <Link to="/orders">My Orders</Link>}
          {user && isAdmin && <Link to="/admin">Admin</Link>}

          {!isAdmin && (
            <Link to="/cart" className="navbar__cart">
              Cart{count > 0 && <span className="cart-badge">{count}</span>}
            </Link>
          )}

          {!user ? (
            <>
              <Link to="/login">Login</Link>
              <Link to="/register" className="btn btn--primary btn--sm">Sign Up</Link>
            </>
          ) : (
            <>
              <span className="navbar__user">Hi, {user.name.split(' ')[0]}</span>
              <button className="btn btn--ghost btn--sm" onClick={handleLogout}>Logout</button>
            </>
          )}
        </nav>
      </div>
    </header>
  )
}
