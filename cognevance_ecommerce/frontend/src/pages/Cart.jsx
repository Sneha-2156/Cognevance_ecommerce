import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function Cart() {
  const { items, updateQuantity, removeItem, total } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()

  const handleCheckout = () => {
    if (!user) {
      navigate('/login')
      return
    }
    navigate('/checkout')
  }

  return (
    <section className="section">
      <h2 className="section__title">Your Cart</h2>

      {items.length === 0 ? (
        <p>Your cart is empty. <Link to="/products">Browse products</Link>.</p>
      ) : (
        <>
          <div className="cart-list">
            {items.map((item) => (
              <div className="cart-item" key={item.productId}>
                {item.imageUrl ? <img src={item.imageUrl} alt={item.name} /> : <div className="product-card__placeholder cart-item__placeholder" />}
                <div className="cart-item__info">
                  <h3>{item.name}</h3>
                  <p>₹{item.price} each</p>
                </div>
                <input type="number" min="1" value={item.quantity}
                  onChange={(e) => updateQuantity(item.productId, Number(e.target.value))} />
                <p className="cart-item__subtotal">₹{(item.price * item.quantity).toFixed(2)}</p>
                <button className="btn btn--ghost btn--sm" onClick={() => removeItem(item.productId)}>Remove</button>
              </div>
            ))}
          </div>

          <div className="cart-summary">
            <p>Total: <strong>₹{total.toFixed(2)}</strong></p>
            <button className="btn btn--primary" onClick={handleCheckout}>Proceed to Checkout</button>
          </div>
        </>
      )}
    </section>
  )
}
