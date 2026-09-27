import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { api } from '../api/client.js'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function ProductDetail() {
  const { id } = useParams()
  const { addItem } = useCart()
  const { isAdmin } = useAuth()
  const [product, setProduct] = useState(null)
  const [quantity, setQuantity] = useState(1)
  const [message, setMessage] = useState('')

  useEffect(() => {
    api.getProduct(id).then(setProduct).catch((err) => setMessage(err.message))
  }, [id])

  if (!product) return <section className="section"><p>{message || 'Loading...'}</p></section>

  const handleAdd = () => {
    addItem(product, quantity)
    setMessage('Added to cart.')
  }

  return (
    <section className="section product-detail">
      <Link to="/products" className="back-link">&larr; Back to Products</Link>
      <div className="product-detail__layout">
        <div className="product-detail__image">
          {product.imageUrl ? <img src={product.imageUrl} alt={product.name} /> : <div className="product-card__placeholder" />}
        </div>
        <div className="product-detail__info">
          {product.category && <span className="tag">{product.category.name}</span>}
          <h2>{product.name}</h2>
          <p className="product-detail__price">₹{product.price}</p>
          <p>{product.description}</p>
          <p className="product-card__stock">{product.stock > 0 ? `${product.stock} in stock` : 'Out of stock'}</p>

          {!isAdmin && product.stock > 0 && (
            <div className="product-detail__actions">
              <input type="number" min="1" max={product.stock} value={quantity}
                onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))} />
              <button className="btn btn--primary" onClick={handleAdd}>Add to Cart</button>
            </div>
          )}
          {message && <p className="form-status">{message}</p>}
        </div>
      </div>
    </section>
  )
}
