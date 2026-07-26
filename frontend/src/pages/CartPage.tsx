import React, { useState, useEffect, useRef } from "react";
import api from "../lib/api"; 

interface CartItem {
  id: string;
  productId: string;
  productName: string;
  productImage: string;
  unitPrice: number;
  originalPrice?: number; // ✅ Ditambahkan untuk menampung harga asli/coret
  quantity: number;
  subtotal: number;
  maxStock: number;
  storeId: string;
  storeName: string;
  sellerActive?: boolean; // ✅ Flag status keaktifan seller
}

interface CartPageProps {
  onBackToCatalog: () => void;
  onCheckout: (checkedItems: any[], pendingOrderData?: any) => void;
  onRefreshCartCount: () => void;
  onProductClick: (productId: string) => void;
}

const CartPage: React.FC<CartPageProps> = ({ onBackToCatalog, onCheckout, onRefreshCartCount, onProductClick }) => {
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [checkoutError, setCheckoutError] = useState<string | null>(null);
  const [checkedItemIds, setCheckedItemIds] = useState<string[]>([]);
  const debounceTimers = useRef<Map<string, ReturnType<typeof setTimeout>>>(new Map());

  const fetchCartData = async () => {
    const token = localStorage.getItem("jatistore_token");
    
    if (!token) {
      setCartItems([]);
      setIsLoading(false);
      return;
    }

    try {
      setIsLoading(true);
      const response = await api.get("/api/v1/cart");
      if (response.data && (response.data.restApiResponseHttpCode === 200 || response.data.code === 200)) {
        const responseData = response.data.restApiResponseData || response.data.data;
        const items: CartItem[] = responseData?.items || [];

        const sortedItems = [...items].sort((a, b) => {
          const storeA = a.storeName || "";
          const storeB = b.storeName || "";
          return storeA.localeCompare(storeB) || a.productName.localeCompare(b.productName);
        });

        setCartItems(sortedItems);

        const activeSortedItems = sortedItems.filter(item => item.sellerActive !== false);
        if (activeSortedItems.length > 0) {
          const firstStoreName = activeSortedItems[0].storeName;
          const firstStoreItems = activeSortedItems
            .filter(item => item.storeName === firstStoreName)
            .map(item => item.id);

          if (checkedItemIds.length === 0) {
            setCheckedItemIds(firstStoreItems);
          }
        } else {
          setCheckedItemIds([]);
        }
      }
    } catch (error) {
      console.error("Gagal mengambil data keranjang:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCartData();
  }, []);

  const updateQuantity = async (type: "increment" | "decrement", currentQuantity: number, cartItemId: string) => {
    if (type === "decrement" && currentQuantity <= 1) {
      await removeItem(cartItemId);
      return;
    }

    setCartItems((prevItems) =>
      prevItems.map((item) =>
        item.id === cartItemId
          ? { ...item, quantity: type === "increment" ? item.quantity + 1 : item.quantity - 1 }
          : item
      )
    );

    const existingTimer = debounceTimers.current.get(cartItemId);
    if (existingTimer) {
      clearTimeout(existingTimer);
    }

    const timer = setTimeout(async () => {
      try {
        const item = cartItems.find(i => i.id === cartItemId);
        if (!item) return;

        const newQuantity = type === "increment" ? currentQuantity + 1 : currentQuantity - 1;
        const response = await api.patch(`/api/v1/cart/items/${cartItemId}`, {
          quantity: newQuantity
        });

        if (response.data && (response.data.restApiResponseHttpCode === 200 || response.data.code === 200)) {
          onRefreshCartCount();
        } else {
          fetchCartData();
        }
      } catch (error) {
        console.error("Gagal memperbarui kuantitas:", error);
        fetchCartData();
      } finally {
        debounceTimers.current.delete(cartItemId);
      }
    }, 500);

    debounceTimers.current.set(cartItemId, timer);
  };

  const removeItem = async (cartItemId: string) => {
    try {
      const response = await api.delete(`/api/v1/cart/items/${cartItemId}`);
      if (response.data && (response.data.restApiResponseHttpCode === 200 || response.data.code === 200)) {
        setCheckedItemIds(prev => prev.filter(id => id !== cartItemId));
        await fetchCartData();
        onRefreshCartCount();
      }
    } catch (error) {
      console.error("Gagal menghapus item:", error);
    }
  };

  const handleToggleCheck = (cartItemId: string) => {
    const itemToToggle = cartItems.find(item => item.id === cartItemId);
    if (!itemToToggle || itemToToggle.sellerActive === false) return;

    const currentStoreId = itemToToggle.storeId;

    setCheckedItemIds((prev) => {
      const isCurrentlyChecked = prev.includes(cartItemId);

      if (isCurrentlyChecked) {
        return prev.filter((id) => id !== cartItemId);
      } else {
        const currentlyCheckedItems = cartItems.filter((item) => prev.includes(item.id));
        const hasDifferentStore = currentlyCheckedItems.some(
          (item) => item.storeId !== currentStoreId
        );

        if (hasDifferentStore) {
          return [cartItemId];
        } else {
          return [...prev, cartItemId];
        }
      }
    });
  };

  const handleToggleStoreCheck = (storeName: string, isChecked: boolean) => {
    const storeItemIds = cartItems
      .filter((item) => item.storeName === storeName && item.sellerActive !== false)
      .map((item) => item.id);

    if (isChecked) {
      setCheckedItemIds(storeItemIds);
    } else {
      setCheckedItemIds([]);
    }
  };

  const subtotal = cartItems
    .filter(item => checkedItemIds.includes(item.id))
    .reduce((acc, item) => {
      const harga = item.unitPrice || 0;
      return acc + (harga * item.quantity);
    }, 0);

  const activeItems = cartItems.filter(item => item.sellerActive !== false);
  const inactiveItems = cartItems.filter(item => item.sellerActive === false);

  const activeStores = Array.from(
    new Set(activeItems.map((item) => item.storeName).filter(Boolean))
  );

  const handleProceedToCheckout = async () => {
    if (checkedItemIds.length === 0 || isSubmitting) return;

    try {
      setIsSubmitting(true);
      setCheckoutError(null);

      const response = await api.post("/api/v1/orders", {
        selectedCartItemIds: checkedItemIds,
      });

      const responseCode = response.data?.code ?? response.data?.restApiResponseHttpCode;
      const createOrderData = response.data?.data ?? response.data?.restApiResponseData;

      if ((response.status === 200 || response.status === 201) && (responseCode === 200 || responseCode === 201) && createOrderData) {
        const itemsToCheckout = cartItems
          .filter(item => checkedItemIds.includes(item.id) && item.sellerActive !== false)
          .map(item => ({
            id: item.productId,
            cartItemId: item.id,
            name: item.productName,
            price: item.unitPrice,
            originalPrice: item.originalPrice,
            image: item.productImage,
            quantity: item.quantity
          }));

        onCheckout(itemsToCheckout, createOrderData);
      } else {
        const errMsg = response.data?.message || response.data?.restApiResponseMessage || "Gagal membuat pesanan.";
        setCheckoutError(errMsg);
      }
    } catch (err: any) {
      console.error("Gagal membuat pesanan:", err);
      const errMsg = err.response?.data?.message || err.response?.data?.restApiResponseMessage || "Gagal membuat pesanan. Silakan coba lagi.";
      setCheckoutError(errMsg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="flex-1 w-full max-w-container-max mx-auto px-margin-mobile md:px-margin-desktop py-stack-lg">
      
      <button 
        onClick={onBackToCatalog} 
        className="text-primary font-label-md text-label-md hover:underline flex items-center gap-1 mb-4 self-start"
      >
        <span className="material-symbols-outlined text-[18px]">arrow_back</span> Back to Shopping
      </button>

      <header className="mb-stack-lg border-b border-outline-variant pb-4">
        <h1 className="font-headline-lg text-headline-lg text-on-background font-bold">Your Cart</h1>
        <p className="font-body-md text-body-md text-on-surface-variant mt-unit">Review your cart items before proceeding to checkout</p>
      </header>

      {checkoutError && (
        <div className="bg-error-container text-on-error-container p-stack-md rounded-lg mb-stack-lg flex items-start gap-3 border border-error/20">
          <span className="material-symbols-outlined text-[20px]">error</span>
          <span className="font-label-md">{checkoutError}</span>
        </div>
      )}

      {isLoading ? (
        <div className="text-center py-12 font-body-md text-on-surface-variant">load item from database...</div>
      ) : cartItems.length === 0 ? (
        <div className="text-center py-12 bg-surface-container-lowest border border-outline-variant rounded-lg">
          <span className="material-symbols-outlined text-[48px] text-on-surface-variant/40">shopping_cart_off</span>
          <p className="text-body-lg font-body-lg text-on-surface-variant mt-2">Keranjang belanja Anda kosong</p>
          <button onClick={onBackToCatalog} className="mt-4 px-6 py-2 bg-primary text-on-primary rounded-full text-label-md font-label-md shadow-sm hover:bg-primary/90 transition-colors">Lihat Produk</button>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-gutter items-start">
          
          <div className="lg:col-span-8 flex flex-col gap-stack-md">
            {activeStores.map((storeName) => {
              const storeItems = activeItems.filter((item) => item.storeName === storeName);
              const isAllStoreItemsChecked = storeItems.every(item => checkedItemIds.includes(item.id));

              return (
                <section key={storeName} className="flex flex-col gap-stack-sm mb-6 bg-surface-container-lowest border border-outline-variant rounded-xl p-4 shadow-sm">
                  <div className="flex items-center gap-2 py-2 border-b border-outline-variant mb-2">
                    <input 
                      type="checkbox"
                      checked={isAllStoreItemsChecked}
                      onChange={(e) => handleToggleStoreCheck(storeName || "", e.target.checked)}
                      className="w-5 h-5 rounded border-outline-variant text-primary focus:ring-primary cursor-pointer mr-2" 
                    />
                    <span className="material-symbols-outlined text-primary">storefront</span>
                    <h2 className="font-bold text-primary text-body-lg">{storeName}</h2>
                  </div>

                  {storeItems.map((item) => (
                    <article key={item.id} className="border-b border-outline-variant last:border-none py-4 flex flex-col sm:flex-row gap-gutter relative">
                      <div className="flex items-center justify-center">
                        <input 
                          type="checkbox" 
                          checked={checkedItemIds.includes(item.id)}
                          onChange={() => handleToggleCheck(item.id)}
                          className="w-5 h-5 rounded border-outline-variant text-primary focus:ring-primary cursor-pointer" 
                        />
                      </div>
                      <div
                        onClick={() => onProductClick(item.productId)}
                        className="w-full sm:w-32 h-32 flex-shrink-0 rounded bg-surface-container-low overflow-hidden border border-outline-variant cursor-pointer hover:opacity-80 transition-opacity"
                      >
                        <img alt={item.productName} className="w-full h-full object-cover" src={item.productImage || "https://placehold.co/150"} />
                      </div>
                      <div className="flex-1 flex flex-col justify-between">
                        <div>
                          <div className="flex justify-between items-start gap-stack-sm">
                            <h3
                              onClick={() => onProductClick(item.productId)}
                              className="font-headline-md text-body-lg text-on-background font-bold cursor-pointer hover:text-primary transition-colors"
                            >
                              {item.productName}
                            </h3>
                            <button onClick={() => removeItem(item.id)} className="text-on-surface-variant hover:text-error transition-colors p-1">
                              <span className="material-symbols-outlined text-[20px]">close</span>
                            </button>
                          </div>
                          <p className="text-body-sm text-on-surface-variant text-[13px] mt-1">Stok Tersedia: {item.maxStock || 0}</p>
                        </div>

                        <div className="flex flex-wrap items-end justify-between mt-4 gap-stack-md">
                          <div className="flex items-center border border-outline-variant rounded bg-surface h-10 w-32">
                            <button
                              onClick={() => updateQuantity("decrement", item.quantity, item.id)}
                              className="w-10 h-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container"
                            >
                              <span className="material-symbols-outlined text-[18px]">remove</span>
                            </button>
                            <input readOnly className="w-12 h-full text-center border-none bg-transparent font-mono-data text-mono-data p-0 focus:ring-0" type="text" value={item.quantity} />
                            <button
                              onClick={() => updateQuantity("increment", item.quantity, item.id)}
                              disabled={item.quantity >= (item.maxStock || 99)}
                              className="w-10 h-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container disabled:opacity-30"
                            >
                              <span className="material-symbols-outlined text-[18px]">add</span>
                            </button>
                          </div>
                          
                          <div className="text-right">
                            {item.originalPrice ? (
                              <div className="flex flex-col items-end">
                                {/* Harga Asli Dicoret */}
                                <span className="font-body-sm text-body-sm text-on-surface-variant line-through">
                                  Rp {(item.originalPrice * item.quantity).toLocaleString("id-ID")}
                                </span>
                                {/* Harga Flash Sale (Warna Merah) */}
                                <span className="font-headline-md text-headline-md text-red-600 font-bold">
                                  Rp {(item.unitPrice * item.quantity).toLocaleString("id-ID")}
                                </span>
                              </div>
                            ) : (
                              /* Harga Normal (Warna Utama / Hijau) */
                              <p className="font-headline-md text-headline-md text-primary font-bold">
                                Rp {(item.unitPrice * item.quantity).toLocaleString("id-ID")}
                              </p>
                            )}
                          </div>
                        </div>
                      </div>
                    </article>
                  ))}
                </section>
              );
            })}

            {/* Inactive Items Section */}
            {inactiveItems.length > 0 && (
              <section className="flex flex-col gap-stack-sm mb-6 bg-surface-container-lowest border border-outline-variant rounded-xl p-4 shadow-sm">
                <div className="flex items-center gap-2 py-2 border-b border-outline-variant mb-2 text-error">
                  <span className="material-symbols-outlined">storefront</span>
                  <h2 className="font-bold text-body-lg">Inactive Store Items (Cannot be processed)</h2>
                </div>

                {inactiveItems.map((item) => (
                  <article key={item.id} className="border-b border-outline-variant last:border-none py-4 flex flex-col sm:flex-row gap-gutter relative bg-surface-container-low/30 px-3 rounded-lg mb-2 opacity-70">
                    <div className="flex items-center justify-center">
                      <input 
                        type="checkbox" 
                        disabled
                        checked={false}
                        className="w-5 h-5 rounded border-outline-variant text-primary cursor-not-allowed opacity-30" 
                      />
                    </div>
                    <div
                      className="w-full sm:w-32 h-32 flex-shrink-0 rounded bg-surface-container-low overflow-hidden border border-outline-variant opacity-50"
                    >
                      <img alt={item.productName} className="w-full h-full object-cover grayscale" src={item.productImage || "https://placehold.co/150"} />
                    </div>
                    <div className="flex-1 flex flex-col justify-between">
                      <div>
                        <div className="flex justify-between items-start gap-stack-sm">
                          <h3
                            className="font-headline-md text-body-lg text-on-surface-variant font-bold cursor-not-allowed"
                          >
                            {item.productName}
                          </h3>
                          <button onClick={() => removeItem(item.id)} className="text-on-surface-variant hover:text-error transition-colors p-1">
                            <span className="material-symbols-outlined text-[20px]">close</span>
                          </button>
                        </div>
                        <p className="text-error text-[12px] font-medium mt-1">This store is currently inactive. You can delete this item from your cart.</p>
                      </div>

                      <div className="flex flex-wrap items-end justify-between mt-4 gap-stack-md">
                        <div className="flex items-center border border-outline-variant rounded bg-surface-container-low h-10 w-32 opacity-50 cursor-not-allowed">
                          <button disabled className="w-10 h-full flex items-center justify-center text-on-surface-variant/40">
                            <span className="material-symbols-outlined text-[18px]">remove</span>
                          </button>
                          <input readOnly disabled className="w-12 h-full text-center border-none bg-transparent font-mono-data text-mono-data p-0 text-on-surface-variant/40" type="text" value={item.quantity} />
                          <button disabled className="w-10 h-full flex items-center justify-center text-on-surface-variant/40">
                            <span className="material-symbols-outlined text-[18px]">add</span>
                          </button>
                        </div>
                        
                        <div className="text-right opacity-50">
                          <p className="font-headline-md text-headline-md text-on-surface-variant font-bold">
                            Rp {(item.unitPrice * item.quantity).toLocaleString("id-ID")}
                          </p>
                        </div>
                      </div>
                    </div>
                  </article>
                ))}
              </section>
            )}
          </div>

          <div className="lg:col-span-4 sticky top-24">
            <div className="bg-surface-container-lowest border border-outline-variant rounded-lg p-stack-md shadow-sm flex flex-col gap-stack-md">
              <h2 className="font-headline-md text-headline-md text-on-background border-b border-outline-variant pb-stack-sm font-bold">Order Summary</h2>
              <div className="flex flex-col gap-stack-sm font-body-md text-body-md">
                <div className="flex justify-between text-on-surface-variant">
                  <span>Subtotal</span>
                  <span>Rp {subtotal.toLocaleString("id-ID")}</span>
                </div>
              </div>
              <div className="border-t border-outline-variant pt-stack-sm flex justify-between items-center">
                <span className="font-headline-md text-on-background font-bold">Grand Total</span>
                <span className="font-display-lg text-[22px] font-bold text-primary">Rp {subtotal.toLocaleString("id-ID")}</span>
              </div>
              <button 
                onClick={handleProceedToCheckout}
                disabled={checkedItemIds.length === 0 || isSubmitting}
                className="w-full bg-primary hover:bg-primary/90 text-on-primary font-label-md text-label-md py-3 px-6 rounded-full transition-colors flex items-center justify-center gap-2 mt-stack-sm shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isSubmitting ? "Creating Order..." : "Checkout"}
                <span className="material-symbols-outlined">
                  {isSubmitting ? "progress_activity" : "arrow_forward"}
                </span>
              </button>
            </div>
          </div>

        </div>
      )}
    </main>
  );
};

export default CartPage;