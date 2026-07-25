import { StrictMode, useState, useEffect } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Routes, Route, useNavigate, useLocation } from "react-router-dom";
import CatalogPage from "./pages/CatalogPage";
import ProductDetailPage from "./pages/ProductDetailPage";
import CartPage from "./pages/CartPage";
import CheckoutPage from "./pages/CheckoutPage";
import OrderHistoryPage from "./pages/OrderHistoryPage";
import UserProfilePage from "./pages/UserProfilePage";
import UserFinancialsPage from "./pages/UserFinancialsPage";
import { UserLayout } from "./components/layout/user/UserLayout";

import api from "./lib/api";
import { authService } from "./service/auth/authService";
import { useAuthStore } from "./store/auth/useAuthStore";

import "./App.css";
import Login from "./container/auth/Login";
import Register from "./container/auth/Register";
import Dashboard from "./container/seller/Dashboard";
import ProductManagement from "./container/seller/Product";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import OrderFulfillment from "./container/seller/OrderFulfillment";
import { Outlet } from "react-router-dom";

import SellerFlashSaleManager from "./container/seller/FlashSaleManager";
import Financials from "./container/seller/Financials";
const App = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [currentPage, setCurrentPage] = useState<"catalog" | "detail" | "cart"| "checkout" | "history">(
    (location.state?.returnToPage as any) || "catalog"
  );
  const [selectedProduct, setSelectedProduct] = useState<string | null>(null);
  
  // ⚡ State untuk melacak apakah produk yang diklik berasal dari flash sale
  const [isCurrentProductFlashSale, setIsCurrentProductFlashSale] = useState<boolean>(false);

  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false);
  const [cartCount, setCartCount] = useState<number>(0);
  const [pendingProductId, setPendingProductId] = useState<string | null>(
    location.state?.pendingProductId || null
  );
  const [selectedCheckoutItems, setSelectedCheckoutItems] = useState<any[]>([]);

  // STATE PENCARIAN GLOBAL
  const [catalogSearchQuery, setCatalogSearchQuery] = useState<string>("");

  // Sync isLoggedIn with localStorage token (check correct key: jatistore_token)
  useEffect(() => {
    const token = localStorage.getItem("jatistore_token");
    setIsLoggedIn(!!token);
  }, []);

  useEffect(() => {
    fetchCartCount();
  }, []);

  const handleNavigateToCart = () => {
    if (!isLoggedIn) {
      navigate("/auth/login", { state: { returnToPage: "cart" } });
    } else {
      navigate("/");
      setCurrentPage("cart");
    }
  };

  const handleAddToCart = async (productId: string, quantity: number = 1) => {
    const token = localStorage.getItem("jatistore_token");

    if (!token) {
      navigate("/auth/login", { state: { returnToPage: "cart", pendingProductId: productId } });
      return;
    }

    try {
      const response = await api.post("/api/v1/cart/items", {
        productId: productId,
        quantity: quantity
      });

      if (response.data && (response.data.restApiResponseHttpCode === 200 || response.data.code === 200)) {
        fetchCartCount();
      }
    } catch (error: any) {
      console.error("Gagal menambahkan ke keranjang:", error);
      const errorMessage = error.response?.data?.message || error.response?.data?.restApiResponseMessage || "Terjadi kesalahan pada server.";
      alert(`Gagal: ${errorMessage}`);
    }
  };

  const fetchCartCount = async () => {
    const token = localStorage.getItem("jatistore_token");
    if (!token) {
      setCartCount(0);
      return;
    }

    try {
      const response = await api.get("/api/v1/cart");
      if (response.data && (response.data.restApiResponseHttpCode === 200 || response.data.code === 200)) {
        const responseData = response.data.restApiResponseData || response.data.data;
        const items = responseData?.items || [];
        const totalQuantity = items.reduce((sum: number, item: any) => sum + item.quantity, 0);
        setCartCount(totalQuantity);
      }
    } catch (error) {
      console.error("Gagal mengambil jumlah keranjang:", error);
    }
  };


  useEffect(() => {
    if (isLoggedIn && pendingProductId) {
      handleAddToCart(pendingProductId, 1).then(() => {
        setPendingProductId(null);
        navigate(".", { replace: true, state: { ...location.state, pendingProductId: null } });
      });
    }
  }, [isLoggedIn, pendingProductId]);

  return (
    <div className="text-on-background bg-background min-h-screen flex flex-col font-sans antialiased">
      
        <nav className="bg-surface border-b border-outline-variant shadow-sm w-full sticky top-0 z-50">
          <div className="flex justify-between items-center w-full px-margin-mobile md:px-margin-desktop max-w-container-max mx-auto h-16 gap-4">

            {/* Sebelah Kiri: Logo */}
            <div className="flex items-center flex-shrink-0">
              <button
                onClick={() => {
                  setCatalogSearchQuery("");
                  navigate("/");
                  setCurrentPage("catalog");
                }}
                className="text-headline-md font-headline-lg font-bold text-primary hover:opacity-80 transition-opacity"
              >
                JatiStore
              </button>
            </div>

            {/* Bagian Search Bar Header Telah Dihapus Sesuai Permintaan agar Tidak Membebani BE */}
            <div className="flex-1"></div>

            {/* Sebelah Kanan: Menu Navigasi Kondisional */}
            <div className="flex items-center gap-stack-md flex-shrink-0">
              {currentPage === "checkout" ? (
                isLoggedIn && (
                  <button
                    onClick={() => navigate("/user/profile")}
                    className="flex items-center gap-1 font-label-md text-label-md text-on-surface hover:text-primary transition-colors p-2 rounded-full hover:bg-surface-container"
                  >
                    <span className="material-symbols-outlined text-[24px]">account_circle</span>
                    <span className="hidden sm:inline font-medium">Profil</span>
                  </button>
                )
              ) : currentPage !== "cart" ? (
                <>
                  <button
                    onClick={handleNavigateToCart}
                    className="p-2 rounded-full hover:bg-surface-container text-on-surface-variant hover:text-primary transition-colors relative"
                  >
                    <span className="material-symbols-outlined">shopping_cart</span>
                    {cartCount > 0 && (
                      <span className="absolute top-0 right-0 bg-error text-on-error font-label-sm text-xs flex items-center justify-center min-w-[18px] h-[18px] rounded-full px-1 shadow-sm">
                        {cartCount}
                      </span>
                    )}
                  </button>

                  <div className="h-6 w-px bg-outline-variant"></div>

                  {!isLoggedIn ? (
                    <button onClick={() => navigate("/auth/login", { state: { returnToPage: currentPage } })} className="bg-primary text-on-primary font-label-md text-label-md px-4 py-1.5 rounded-full hover:bg-primary/90 transition-colors shadow-sm">
                      Login
                    </button>
                  ) : (
                    <>
                      <button
                        onClick={() => navigate("/user/profile")}
                        className="flex items-center gap-1 font-label-md text-label-md text-on-surface hover:text-primary transition-colors p-2 rounded-full hover:bg-surface-container"
                      >
                        <span className="material-symbols-outlined text-[24px]">account_circle</span>
                        <span className="hidden sm:inline font-medium">Profile</span>
                      </button>
                      <button
                        onClick={async () => {
                          try {
                            await authService.logout();
                            useAuthStore.getState().logout();
                            setIsLoggedIn(false);
                            setCartCount(0);
                            navigate("/");
                            setCurrentPage("catalog");
                          } catch (error: any) {
                            console.error("Logout error:", error);
                            if (error.response?.status === 401 || error.response?.status === 403) {
                              useAuthStore.getState().logout();
                              setIsLoggedIn(false);
                              setCartCount(0);
                              navigate("/");
                              setCurrentPage("catalog");
                            } else {
                              alert("Logout failed due to a network or server error. Please try again.");
                            }
                          }
                        }}
                        className="flex items-center gap-1 font-label-md text-label-md text-error font-medium hover:underline ml-2"
                      >
                        <span className="material-symbols-outlined text-[18px]">logout</span>
                        <span>Logout</span>
                      </button>
                    </>
                  )}
                </>
              ) : null}
            </div>
          </div>
        </nav>

      {/* RENDER KONTEN HALAMAN */}
      {location.pathname.startsWith("/user") ? (
        <Outlet />
      ) : (
        <>
          {currentPage === "catalog" && (
            <CatalogPage 
              onProductClick={(productId, isFlashSale = false) => {
            setSelectedProduct(productId);
            setIsCurrentProductFlashSale(isFlashSale);
            setCurrentPage("detail");
          }}
          onCartClick={handleNavigateToCart}
          onAddToCart={handleAddToCart} 
          searchQuery={catalogSearchQuery}
          onCheckout={async (products) => {
            handleAddToCart(products[0].id, 1);
            try {
              const response = await api.get("/api/v1/cart");
              if (response.data && (response.data.restApiResponseHttpCode === 200 || response.data.code === 200)) {
                const responseData = response.data.restApiResponseData || response.data.data;
                const items = responseData?.items || [];
                const item = items.find((i: any) => i.productId === products[0].id);
                if (item) {
                  setSelectedCheckoutItems([{
                    id: products[0].id,
                    cartItemId: item.id,
                    name: products[0].name,
                    price: products[0].price,
                    image: products[0].image,
                    quantity: 1
                  }]);
                  setCurrentPage("checkout");
                }
              }
            } catch (error) {
              console.error("Gagal mengambil jumlah keranjang:", error);
            }
          }}
        />
      )}

      {currentPage === "detail" && selectedProduct && (
        <ProductDetailPage 
          productId={selectedProduct} 
          onBackToCatalog={() => setCurrentPage("catalog")} 
          onAddToCart={handleAddToCart}
          onCartClick={handleNavigateToCart}
          isFromFlashSale={isCurrentProductFlashSale}
        />
      )}

      {currentPage === "cart" && (
        <CartPage
          onBackToCatalog={() => setCurrentPage("catalog")}
          onCheckout={(itemsToCheckout) => {
            setSelectedCheckoutItems(itemsToCheckout);
            setCurrentPage("checkout");
          }}
          onRefreshCartCount={fetchCartCount}
          onProductClick={(productId, isFlashSale = false) => {
            setSelectedProduct(productId);
            setIsCurrentProductFlashSale(isFlashSale);
            setCurrentPage("detail");
          }}
        />
      )}

      {currentPage === "checkout" && (
        <CheckoutPage 
          cartItems={selectedCheckoutItems}
          onBackToCart={() => setCurrentPage("cart")}
          onPaymentSuccess={() => {
            setSelectedCheckoutItems([]);
            fetchCartCount();
            navigate("/user/orders");
          }}
        />
      )}
        </>
      )}
    </div>
  );
};

import { AdminLayout } from "./components/layout/admin/AdminLayout.tsx";
import { Dashboard as AdminDashboard } from "./container/admin/Dashboard.tsx";
import { AuditTrails } from "./container/admin/AuditTrails.tsx";
import { FlashSaleManager } from "./container/admin/FlashSaleManager";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<App />}>
          <Route path="user" element={<ProtectedRoute allowedRoles={["USER"]} />}>
            <Route element={<UserLayout />}>
              <Route path="profile" element={<UserProfilePage />} />
              <Route path="financials" element={<UserFinancialsPage />} />
              <Route path="orders" element={<OrderHistoryPage />} />
            </Route>
          </Route>
          <Route path="*" element={<></>} />
        </Route>
        
        <Route path="/auth/login" element={<Login />} />
        <Route path="/auth/register" element={<Register />} />

        {/* Seller Routes */}
        <Route element={<ProtectedRoute allowedRoles={["SELLER"]} />}>
          <Route path="/seller/dashboard" element={<Dashboard />} />
          <Route path="/seller/products" element={<ProductManagement />} />
          <Route path="/seller/orders" element={<OrderFulfillment />} />
          <Route path="/seller/flash-sales" element={<SellerFlashSaleManager />} />
          <Route path="/seller/financials" element={<Financials />} />
        </Route>

        {/* Admin Routes */}
        <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
          <Route element={<AdminLayout />}>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
            <Route path="/admin/audit-trails" element={<AuditTrails />} />
            <Route path="/admin/flash-sales" element={<FlashSaleManager />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  </StrictMode>,
);