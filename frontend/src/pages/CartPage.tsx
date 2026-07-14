import React from "react";
import { type CartItem } from "../data/productsMock";

interface CartPageProps {
  cartItems: CartItem[];
  setCartItems: React.Dispatch<React.SetStateAction<CartItem[]>>;
  onBackToCatalog: () => void;
  onLogout: () => void;
  onCheckout: () => void;
}

const CartPage: React.FC<CartPageProps> = ({ cartItems, setCartItems, onBackToCatalog, onLogout, onCheckout }) => {
  const updateQuantity = (id: string, type: "increment" | "decrement") => {
    setCartItems(prev => prev.map(item => {
      if (item.id === id) {
        if (type === "increment" && item.quantity < item.maxStock) {
          return { ...item, quantity: item.quantity + 1 };
        } else if (type === "decrement" && item.quantity > 1) {
          return { ...item, quantity: item.quantity - 1 };
        }
      }
      return item;
    }));
  };

  const removeItem = (id: string) => {
    setCartItems(prev => prev.filter(item => item.id !== id));
  };

  const subtotal = cartItems.reduce((acc, item) => acc + (item.price * item.quantity), 0);
  const stores = Array.from(new Set(cartItems.map(item => item.store)));

  return (
    <>
      {/* TopNavBar */}
      <nav className="bg-surface border-b border-outline-variant shadow-sm w-full sticky top-0 z-50">
        <div className="flex justify-between items-center w-full px-margin-desktop max-w-container-max mx-auto h-16">
          <div className="flex items-center gap-gutter">
            <button onClick={onBackToCatalog} className="text-headline-md font-headline-lg font-bold text-primary hover:opacity-80">
              JatiStore
            </button>
          </div>
          <div className="flex-1 max-w-md mx-gutter hidden md:block">
            <div className="relative">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant">search</span>
              <input className="w-full pl-10 pr-4 py-2 bg-surface-container-low border border-outline-variant rounded-full font-body-sm text-body-sm focus:outline-none focus:border-primary" placeholder="Search products..." type="text" />
            </div>
          </div>
          <div className="flex items-center gap-stack-md">
            <button onClick={onLogout} className="flex items-center gap-1 text-body-sm text-error font-medium hover:underline">
              <span className="material-symbols-outlined text-[18px]">logout</span> Keluar
            </button>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="flex-1 w-full max-w-container-max mx-auto px-margin-mobile md:px-margin-desktop py-stack-lg">
        <header className="mb-stack-lg flex justify-between items-end">
          <div>
            <h1 className="font-headline-lg text-headline-lg text-on-background">Your Cart</h1>
            <p className="font-body-md text-body-md text-on-surface-variant mt-unit">Review your items before checkout.</p>
          </div>
          <button onClick={onBackToCatalog} className="text-primary font-label-md text-label-md hover:underline flex items-center gap-1">
            <span className="material-symbols-outlined text-[16px]">arrow_back</span> Lanjut Belanja
          </button>
        </header>

        {cartItems.length === 0 ? (
          <div className="text-center py-12 bg-surface-container-lowest border border-outline-variant rounded-lg">
            <span className="material-symbols-outlined text-[48px] text-on-surface-variant/40">shopping_cart_off</span>
            <p className="text-body-lg font-body-lg text-on-surface-variant mt-2">Keranjang belanja Anda kosong</p>
            <button onClick={onBackToCatalog} className="mt-4 px-6 py-2 bg-primary text-on-primary rounded-full text-label-md font-label-md">Lihat Produk</button>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-gutter items-start">
            {/* Cart Items (Left Column) */}
            <div className="lg:col-span-8 flex flex-col gap-stack-md">
              {stores.map(storeName => (
                <section key={storeName} className="flex flex-col gap-stack-sm mb-4">
                  <div className="flex items-center gap-2 py-2 border-b border-outline-variant mb-2">
                    <input defaultChecked className="w-5 h-5 rounded border-outline-variant text-primary focus:ring-primary cursor-pointer mr-2" type="checkbox" />
                    <span className="material-symbols-outlined text-primary">storefront</span>
                    <h2 className="font-bold text-primary text-body-lg">{storeName}</h2>
                  </div>

                  {cartItems.filter(item => item.store === storeName).map(item => (
                    <article key={item.id} className="bg-surface-container-lowest border border-outline-variant rounded-lg p-stack-md flex flex-col sm:flex-row gap-gutter relative hover:shadow-md transition-shadow">
                      <div className="flex items-center justify-center">
                        <input defaultChecked className="w-5 h-5 rounded border-outline-variant text-primary focus:ring-primary cursor-pointer" type="checkbox" />
                      </div>
                      <div className="w-full sm:w-32 h-32 flex-shrink-0 rounded bg-surface-container-low overflow-hidden">
                        <img alt={item.name} className="w-full h-full object-cover" src={item.image} />
                      </div>
                      <div className="flex-1 flex flex-col justify-between">
                        <div>
                          <div className="flex justify-between items-start gap-stack-sm">
                            <h3 className="font-headline-md text-body-lg text-on-background">{item.name}</h3>
                            <button onClick={() => removeItem(item.id)} aria-label="Remove item" className="text-on-surface-variant hover:text-error transition-colors p-1">
                              <span className="material-symbols-outlined text-[20px]">close</span>
                            </button>
                          </div>
                          <p className="font-body-sm text-body-sm text-on-surface-variant mt-unit">{item.variant}</p>
                          
                          {/* Peringatan Stok Maksimal */}
                          {item.quantity >= item.maxStock && (
                            <div className="inline-flex items-center gap-unit mt-unit px-2 py-1 bg-error-container/20 border border-error/30 rounded text-error font-label-sm text-label-sm">
                              <span className="material-symbols-outlined text-[14px]">warning</span>
                              Maximum stock reached
                            </div>
                          )}
                        </div>

                        <div className="flex flex-wrap items-end justify-between mt-stack-md gap-stack-md">
                          <div className="flex flex-col gap-unit">
                            <div className="flex items-center border border-outline-variant rounded bg-surface h-10 w-32">
                              <button onClick={() => updateQuantity(item.id, "decrement")} className="w-10 h-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container transition-colors">
                                <span className="material-symbols-outlined text-[18px]">remove</span>
                              </button>
                              <input readOnly className="w-12 h-full text-center border-none bg-transparent font-mono-data text-mono-data p-0 focus:ring-0" type="text" value={item.quantity} />
                              <button 
                                onClick={() => updateQuantity(item.id, "increment")} 
                                disabled={item.quantity >= item.maxStock}
                                className={`w-10 h-full flex items-center justify-center transition-colors ${item.quantity >= item.maxStock ? "text-on-surface-variant/30 cursor-not-allowed" : "text-on-surface-variant hover:bg-surface-container"}`}
                              >
                                <span className="material-symbols-outlined text-[18px]">add</span>
                              </button>
                            </div>
                          </div>
                          <div className="text-right">
                            {item.originalPrice && (
                              <p className="font-body-sm text-body-sm text-on-surface-variant line-through">Rp {(item.originalPrice * item.quantity).toLocaleString("id-ID")}</p>
                            )}
                            <p className="font-headline-md text-headline-md text-primary">Rp {(item.price * item.quantity).toLocaleString("id-ID")}</p>
                          </div>
                        </div>
                      </div>
                    </article>
                  ))}
                </section>
              ))}
            </div>

            {/* Summary (Right Column) */}
            <div className="lg:col-span-4 sticky top-24">
              <div className="bg-surface-container-lowest border border-outline-variant rounded-lg p-stack-md shadow-sm flex flex-col gap-stack-md">
                <h2 className="font-headline-md text-headline-md text-on-background border-b border-outline-variant pb-stack-sm">Order Summary</h2>
                <div className="flex flex-col gap-stack-sm font-body-md text-body-md">
                  <div className="flex justify-between text-on-surface-variant">
                    <span>Subtotal</span>
                    <span>Rp {subtotal.toLocaleString("id-ID")}</span>
                  </div>
                  <div className="flex justify-between text-on-surface-variant">
                    <span>Shipping</span>
                    <span className="text-body-sm italic">Calculated at checkout</span>
                  </div>
                  <div className="flex justify-between text-on-surface-variant">
                    <span>Tax</span>
                    <span className="text-body-sm italic">Calculated at checkout</span>
                  </div>
                </div>
                <div className="border-t border-outline-variant pt-stack-sm flex justify-between items-center">
                  <span className="font-headline-md text-headline-md text-on-background">Grand Total</span>
                  <span className="font-display-lg text-[24px] font-bold text-primary">Rp {subtotal.toLocaleString("id-ID")}</span>
                </div>
                {/* <button className="w-full bg-primary hover:bg-primary/90 text-on-primary font-label-md text-label-md py-3 px-6 rounded-full transition-colors flex items-center justify-center gap-2 mt-stack-sm shadow-sm hover:shadow-md">
                  Checkout
                  <span className="material-symbols-outlined">arrow_forward</span>
                </button> */}
                <button 
                    onClick={onCheckout}
                    className="w-full bg-primary hover:bg-primary/90 text-on-primary font-label-md text-label-md py-3 px-6 rounded-full transition-colors flex items-center justify-center gap-2 mt-stack-sm shadow-sm hover:shadow-md"
                    >
                    Checkout
                    <span className="material-symbols-outlined">arrow_forward</span>
                </button>
                <div className="flex items-center justify-center gap-unit text-on-surface-variant mt-unit">
                  <span className="material-symbols-outlined text-[16px]">lock</span>
                  <span className="font-label-sm text-label-sm">Secure Checkout</span>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </>
  );
};

export default CartPage;