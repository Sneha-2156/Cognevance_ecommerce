import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { loadStripe } from '@stripe/stripe-js'
import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js'
import { useCart } from '../context/CartContext.jsx'
import { api } from '../api/client.js'

// Set VITE_STRIPE_PUBLISHABLE_KEY in your .env (a "pk_test_..." key from the Stripe dashboard)
const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY || 'pk_test_placeholder')

function CheckoutForm() {
  const { items, total, clearCart } = useCart()
  const stripe = useStripe()
  const elements = useElements()
  const navigate = useNavigate()

  const [address, setAddress] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (!address.trim()) {
      setError('Shipping address is required.')
      return
    }
    if (!stripe || !elements) return

    setLoading(true)
    try {
      // 1. Create the order server-side, which also creates a Stripe PaymentIntent
      const checkout = await api.checkout({
        items: items.map((i) => ({ productId: i.productId, quantity: i.quantity })),
        shippingAddress: address,
      })

      // 2. Confirm the card payment with Stripe.js using the client secret
      const result = await stripe.confirmCardPayment(checkout.clientSecret, {
        payment_method: { card: elements.getElement(CardElement) },
      })

      if (result.error) {
        throw new Error(result.error.message)
      }

      // 3. Tell the backend the payment succeeded so it marks the order PAID
      //    (a production build should rely on a Stripe webhook for this instead)
      await api.confirmPayment(result.paymentIntent.id)

      clearCart()
      navigate('/orders')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <form className="checkout-form" onSubmit={handleSubmit}>
      <div className="form-row">
        <label>Shipping Address</label>
        <textarea rows="3" value={address} onChange={(e) => setAddress(e.target.value)} required />
      </div>

      <div className="form-row">
        <label>Card Details</label>
        <div className="card-element-wrapper">
          <CardElement options={{ style: { base: { fontSize: '16px', color: '#eef0f5' } } }} />
        </div>
        <p className="checkout-hint">Test card: 4242 4242 4242 4242, any future date, any CVC.</p>
      </div>

      {error && <p className="form-status form-status--error">{error}</p>}

      <button className="btn btn--primary" type="submit" disabled={!stripe || loading}>
        {loading ? 'Processing...' : `Pay ₹${total.toFixed(2)}`}
      </button>
    </form>
  )
}

export default function Checkout() {
  const { items, total } = useCart()

  if (items.length === 0) {
    return <section className="section"><p>Your cart is empty.</p></section>
  }

  return (
    <section className="section checkout-page">
      <h2 className="section__title">Checkout</h2>
      <div className="checkout-layout">
        <Elements stripe={stripePromise}>
          <CheckoutForm />
        </Elements>
        <div className="checkout-summary">
          <h3>Order Summary</h3>
          {items.map((i) => (
            <div className="checkout-summary__line" key={i.productId}>
              <span>{i.name} x{i.quantity}</span>
              <span>₹{(i.price * i.quantity).toFixed(2)}</span>
            </div>
          ))}
          <div className="checkout-summary__total">
            <span>Total</span><span>₹{total.toFixed(2)}</span>
          </div>
        </div>
      </div>
    </section>
  )
}
