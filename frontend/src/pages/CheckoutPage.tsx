import React, { useState } from "react";
import { type CartItem } from "../data/productsMock";

interface CheckoutPageProps {
  cartItems: CartItem[];
  onBackToCart: () => void;
  onPaymentSuccess: () => void; 
}

const CheckoutPage: React.FC<CheckoutPageProps> = ({ cartItems, onBackToCart, onPaymentSuccess }) => {
  const [paymentMethod, setPaymentMethod] = useState<"card" | "wallet">("card");
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState<boolean>(false);

  const subtotal = cartItems.reduce((acc, item) => acc + item.price * item.quantity, 0);
  const shipping = cartItems.length > 0 ? 12000 : 0;
  const tax = Math.round(subtotal * 0.11); 
  const total = subtotal + shipping + tax;

  const walletBalance = 500000; 
  const remainingBalance = walletBalance - total;
  const isBalanceEnough = remainingBalance >= 0;

  const handlePayNow = (e: React.FormEvent) => {
    e.preventDefault();
    e.stopPropagation();

    const savedOrders = localStorage.getItem("jatistore_orders");
    const existingOrders = savedOrders ? JSON.parse(savedOrders) : [];

    const newOrders = cartItems.map((item) => ({
        id: Date.now().toString() + Math.random().toString(36).substr(2, 5),
        orderNumber: `#JS-${Math.floor(1000 + Math.random() * 9000)}`,
        datePlaced: new Date().toLocaleDateString("en-US", { 
          month: "short", 
          day: "numeric", 
          year: "numeric" 
        }),
        totalAmount: item.price * item.quantity,
        status: "Shipped",
        productName: item.name,
        variant: "Standard",
        quantity: item.quantity,
        image: item.image,
    }));

    localStorage.setItem("jatistore_orders", JSON.stringify([...newOrders, ...existingOrders]));
    setIsSuccessModalOpen(true);
  };

  return (
    <div className="bg-background text-on-background font-body-md min-h-screen flex flex-col relative">

      {/* 🚀 MAIN CONTENT AREA WITH TOP RETURN BUTTON */}
      <main className="flex-grow w-full max-w-container-max mx-auto px-margin-mobile md:px-margin-desktop py-stack-lg">
        
        {/* Return to Cart button instead of the heavy inner header[cite: 4] */}
        <button 
          onClick={onBackToCart}
          className="flex items-center gap-2 text-primary hover:underline transition-all font-label-md text-label-md mb-6 self-start"
        >
          <span className="material-symbols-outlined text-[18px]" style={{ fontVariationSettings: "'FILL' 0" }}>arrow_back</span>
          Return to Cart
        </button>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
          
          {/* Left Side: Payment Form */}
          <section className="lg:col-span-7 space-y-stack-lg">
            <div>
              <h1 className="font-headline-lg text-headline-lg text-on-background mb-unit font-bold">Secure Checkout</h1>
              <p className="font-body-md text-body-md text-on-surface-variant">Complete your purchase safely and securely.</p>
            </div>

            {/* Payment Method Selector */}
            <div className="bg-surface-container-lowest rounded-xl border border-outline-variant p-stack-lg shadow-sm mb-stack-lg">
              <h2 className="font-headline-md text-headline-md text-on-surface mb-stack-md font-bold">Choose Payment Method</h2>
              <div className="grid grid-cols-2 gap-stack-md">
                
                {/* Card Button */}
                <button 
                  type="button"
                  onClick={() => setPaymentMethod("card")}
                  className={`flex flex-col items-center gap-2 p-stack-md border-2 rounded-xl transition-all ${
                    paymentMethod === "card" 
                      ? "border-primary bg-surface-container-low" 
                      : "border-outline-variant hover:border-primary"
                  }`}
                >
                  <span className={`text-headline-lg material-symbols-outlined ${paymentMethod === "card" ? "text-primary" : "text-outline"}`}>credit_card</span>
                  <span className="font-label-md text-on-surface">Card</span>
                  {paymentMethod === "card" && (
                    <span className="material-symbols-outlined text-primary" id="check-card">check_circle</span>
                  )}
                </button>

                {/* Wallet Button */}
                <button 
                  type="button"
                  onClick={() => setPaymentMethod("wallet")}
                  className={`flex flex-col items-center gap-2 p-stack-md border-2 rounded-xl transition-all ${
                    paymentMethod === "wallet" 
                      ? "border-primary bg-surface-container-low" 
                      : "border-outline-variant hover:border-primary"
                  }`}
                >
                  <span className={`text-headline-lg material-symbols-outlined ${paymentMethod === "wallet" ? "text-primary" : "text-outline"}`}>account_balance_wallet</span>
                  <span className="font-label-md text-on-surface">Jati Wallet</span>
                  {paymentMethod === "wallet" && (
                    <span className="material-symbols-outlined text-primary" id="check-wallet">check_circle</span>
                  )}
                </button>
              </div>
            </div>

            {/* Dynamic Form Content Container */}
            <div className="bg-surface-container-lowest rounded-xl border border-outline-variant p-stack-lg shadow-sm">
              
              {/* CARD VIEW */}
              {paymentMethod === "card" && (
                <div className="transition-opacity duration-300">
                  <h2 className="font-headline-md text-headline-md text-on-surface mb-stack-md flex items-center gap-2 font-bold">
                    <span className="material-symbols-outlined text-primary" style={{ fontVariationSettings: "'FILL' 1" }}>credit_card</span>
                    Payment Details
                  </h2>
                  <form className="space-y-stack-md" onSubmit={handlePayNow}>
                    <div>
                      <label className="block font-label-md text-label-md text-on-surface-variant mb-unit" htmlFor="cardName">Cardholder Name</label>
                      <div className="relative rounded-lg border border-outline-variant bg-surface-bright transition-all input-focus-ring">
                        <input 
                          className="w-full bg-transparent border-none font-body-md text-body-md text-on-surface py-2 px-3 focus:ring-0" 
                          id="cardName" 
                          placeholder="Jane Doe" 
                          required 
                          type="text" 
                        />
                      </div>
                    </div>
                    <div>
                      <label className="block font-label-md text-label-md text-on-surface-variant mb-unit" htmlFor="cardNumber">Card Number</label>
                      <div className="relative rounded-lg border border-outline-variant bg-surface-bright transition-all input-focus-ring flex items-center pr-3">
                        <input 
                          className="w-full bg-transparent border-none font-body-md text-body-md text-on-surface py-2 px-3 focus:ring-0 font-mono-data" 
                          id="cardNumber" 
                          maxLength={19} 
                          placeholder="0000 0000 0000 0000" 
                          required 
                          type="text" 
                        />
                        <span className="material-symbols-outlined text-outline-variant">payment</span>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-stack-md">
                      <div>
                        <label className="block font-label-md text-label-md text-on-surface-variant mb-unit" htmlFor="expiry">Expiry (MM/YY)</label>
                        <div className="relative rounded-lg border border-outline-variant bg-surface-bright transition-all input-focus-ring">
                          <input 
                            className="w-full bg-transparent border-none font-body-md text-body-md text-on-surface py-2 px-3 focus:ring-0 font-mono-data" 
                            id="expiry" 
                            maxLength={5} 
                            placeholder="MM/YY" 
                            required 
                            type="text" 
                          />
                        </div>
                      </div>
                      <div>
                        <label className="block font-label-md text-label-md text-on-surface-variant mb-unit" htmlFor="cvv">CVV</label>
                        <div className="relative rounded-lg border border-outline-variant bg-surface-bright transition-all input-focus-ring flex items-center pr-3">
                          <input 
                            className="w-full bg-transparent border-none font-body-md text-body-md text-on-surface py-2 px-3 focus:ring-0 font-mono-data" 
                            id="cvv" 
                            maxLength={4} 
                            placeholder="123" 
                            required 
                            type="password" 
                          />
                          <span className="material-symbols-outlined text-outline-variant" style={{ fontVariationSettings: "'FILL' 0" }}>help</span>
                        </div>
                      </div>
                    </div>
                    <div className="mt-stack-lg p-stack-sm bg-surface-container rounded-lg flex items-center gap-3">
                      <span className="material-symbols-outlined text-primary" style={{ fontVariationSettings: "'FILL' 1" }}>verified_user</span>
                      <p className="font-body-sm text-body-sm text-on-surface-variant">Your payment is held securely until you confirm delivery.</p>
                    </div>
                  </form>
                </div>
              )}

              {/* WALLET VIEW */}
              {paymentMethod === "wallet" && (
                <div className="transition-opacity duration-300" id="payment-wallet-view">
                  <h2 className="font-headline-md text-headline-md text-on-surface mb-stack-md flex items-center gap-2 font-bold">
                    <span className="material-symbols-outlined text-primary" style={{ fontVariationSettings: "'FILL' 1" }}>account_balance_wallet</span>
                    Wallet Payment
                  </h2>
                  <div className="space-y-stack-md">
                    
                    {isBalanceEnough ? (
                      <div className="mb-stack-md p-stack-sm bg-primary-container/10 border border-primary/20 rounded-lg flex items-center gap-2 text-primary">
                        <span className="material-symbols-outlined text-body-md">check_circle</span>
                        <span className="font-label-md">Sufficient wallet balance available.</span>
                      </div>
                    ) : (
                      <div className="mb-stack-md p-stack-sm bg-error-container/10 border border-error/20 rounded-lg flex items-center gap-2 text-error">
                        <span className="material-symbols-outlined text-body-md">error</span>
                        <span className="font-label-md">Insufficient wallet balance. Please top up.</span>
                      </div>
                    )}

                    <div className="flex justify-between items-center p-stack-md bg-surface-container-low rounded-lg border border-outline-variant">
                      <div className="flex items-center gap-3">
                        <span className="material-symbols-outlined text-primary">account_balance_wallet</span>
                        <div>
                          <p className="font-label-md text-on-surface font-semibold">Jati Wallet</p>
                          <p className="font-body-sm text-on-surface-variant font-mono-data">
                            Available Balance: <span className="text-primary font-bold">Rp {walletBalance.toLocaleString("id-ID")}</span>
                          </p>
                        </div>
                      </div>
                      <span className="material-symbols-outlined text-primary">check_circle</span>
                    </div>

                    <div className="space-y-2 px-1">
                      <div className="flex justify-between text-body-sm">
                        <span className="text-on-surface-variant">Payment Amount</span>
                        <span className="text-on-surface font-mono-data">Rp {total.toLocaleString("id-ID")}</span>
                      </div>
                      <div className="flex justify-between text-body-sm">
                        <span className="text-on-surface-variant">Remaining Balance</span>
                        <span className={`font-mono-data font-semibold ${isBalanceEnough ? "text-primary" : "text-error"}`}>
                          Rp {remainingBalance.toLocaleString("id-ID")}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              )}

            </div>
          </section>

          {/* Right Side: Order Summary */}
          <aside className="lg:col-span-5">
            <div className="bg-surface-container-lowest rounded-xl border border-outline-variant p-stack-lg shadow-sm sticky top-[100px]">
              <h2 className="font-headline-md text-headline-md text-on-surface mb-stack-md border-b border-outline-variant pb-stack-sm font-bold">Order Summary</h2>
              
              {/* Items List */}
              <div className="space-y-stack-md mb-stack-lg max-h-60 overflow-y-auto pr-1">
                {cartItems.map((item) => (
                  <div key={item.id} className="flex items-start gap-stack-md">
                    <div className="w-16 h-16 rounded bg-surface-container overflow-hidden flex-shrink-0 border border-outline-variant">
                      <img className="w-full h-full object-cover" src={item.image} alt={item.name} />
                    </div>
                    <div className="flex-grow">
                      <h4 className="font-label-md text-label-md text-on-surface line-clamp-1 font-semibold">{item.name}</h4>
                      <p className="font-body-sm text-body-sm text-on-surface-variant">Qty: {item.quantity}</p>
                    </div>
                    <span className="font-mono-data text-mono-data text-on-surface font-semibold">
                      Rp {(item.price * item.quantity).toLocaleString("id-ID")}
                    </span>
                  </div>
                ))}
              </div>

              {/* Calculations Total */}
              <div className="space-y-stack-sm border-t border-outline-variant pt-stack-md mb-stack-lg">
                <div className="flex justify-between font-body-sm text-body-sm text-on-surface-variant">
                  <span>Subtotal</span>
                  <span className="font-mono-data">Rp {subtotal.toLocaleString("id-ID")}</span>
                </div>
                <div className="flex justify-between font-body-sm text-body-sm text-on-surface-variant">
                  <span>Shipping (Standard)</span>
                  <span className="font-mono-data">Rp {shipping.toLocaleString("id-ID")}</span>
                </div>
                <div className="flex justify-between font-body-sm text-body-sm text-on-surface-variant">
                  <span>Tax (PPN 11%)</span>
                  <span className="font-mono-data">Rp {tax.toLocaleString("id-ID")}</span>
                </div>
                <div className="flex justify-between font-headline-md text-headline-md text-on-surface mt-stack-md pt-stack-sm border-t border-outline-variant border-dashed">
                  <span className="font-bold">Total</span>
                  <span className="text-primary font-bold text-[20px]">Rp {total.toLocaleString("id-ID")}</span>
                </div>
              </div>

              {/* Pay Now Confirmation Button */}
              <button 
                type="button"
                disabled={paymentMethod === "wallet" && !isBalanceEnough}
                onClick={handlePayNow}
                className={`w-full font-label-md text-label-md py-3 px-4 rounded-full flex items-center justify-center gap-2 transition-all shadow-sm font-semibold ${
                  paymentMethod === "wallet" && !isBalanceEnough
                    ? "bg-outline text-surface cursor-not-allowed opacity-50"
                    : "bg-primary hover:bg-primary/90 text-on-primary"
                }`}
              >
                <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>lock</span>
                Pay Now
              </button>
              <p className="text-center font-label-sm text-label-sm text-outline mt-stack-md flex items-center justify-center gap-1">
                <span className="material-symbols-outlined text-[16px]">shield</span>
                256-bit SSL Encrypted
              </p>
            </div>
          </aside>
        </div>
      </main>

      <footer className="w-full bg-surface border-t border-outline-variant py-stack-lg mt-auto">
        <div className="max-w-container-max mx-auto px-margin-desktop text-center">
          <p className="font-label-sm text-label-sm text-on-surface-variant">© 2026 JatiStore. All rights reserved.</p>
        </div>
      </footer>

      {/* POPUP MODAL: PAYMENT SUCCESSFUL */}
      {isSuccessModalOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-margin-mobile bg-on-background/40 backdrop-blur-sm">
            <div className="bg-surface-container-lowest rounded-[20px] shadow-sm max-w-[480px] w-full overflow-hidden animate-in fade-in zoom-in duration-300">
            <div className="p-stack-lg flex flex-col items-center text-center">
                <div className="w-16 h-16 bg-primary rounded-full flex items-center justify-center mb-stack-md">
                  <span className="material-symbols-outlined text-on-primary text-[40px]" style={{ fontVariationSettings: "'FILL' 1" }}>check_circle</span>
                </div>
                
                <h2 className="font-headline-md text-headline-md font-bold text-on-surface mb-unit">Payment Successful!</h2>
                <p className="font-body-md text-body-md text-on-surface-variant mb-stack-lg">Your payment has been processed successfully.</p>
                
                <div className="w-full bg-surface-container-low border border-outline-variant rounded-xl p-stack-md text-left space-y-2 mb-stack-lg">
                <div className="flex justify-between items-center">
                    <span className="text-label-sm text-on-surface-variant">Transaction ID</span>
                    <span className="text-label-sm font-mono-data text-on-surface">#JT202600123</span>
                </div>
                <div className="flex justify-between items-center">
                    <span className="text-label-sm text-on-surface-variant">Payment Method</span>
                    <span className="text-label-sm text-on-surface capitalize">{paymentMethod}</span>
                </div>
                <div className="flex justify-between items-center">
                    <span className="text-label-sm text-on-surface-variant">Payment Date</span>
                    <span className="text-label-sm text-on-surface">July 16, 2026 • 3:12 PM</span>
                </div>
                <div className="flex justify-between items-center pt-2 border-t border-outline-variant">
                    <span className="text-label-md font-semibold text-on-surface">Amount Paid</span>
                    <span className="text-headline-md font-bold text-primary">Rp {total.toLocaleString("id-ID")}</span>
                </div>
              </div>

              <p className="font-body-sm text-body-sm text-on-surface-variant mb-stack-lg">
                Your order has been confirmed and is now being processed. You can track your order anytime from your Order History page.
              </p>

                <button 
                    type="button"
                    onClick={(e) => {
                        e.preventDefault();
                        setIsSuccessModalOpen(false);
                        if (onPaymentSuccess) {
                            onPaymentSuccess();
                        }
                    }}
                    className="w-full bg-primary hover:opacity-90 text-on-primary font-label-md text-label-md py-3 px-4 rounded-full transition-all shadow-sm font-semibold"
                >
                    Continue to Order History
                </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CheckoutPage;