import { StrictMode, useState, useEffect } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from "react-router-dom";
import CatalogPage from "./pages/CatalogPage";
import ProductDetailPage from "./pages/ProductDetailPage";
import LoginPage from "./pages/LoginPage";
import CartPage from "./pages/CartPage";
import CheckoutPage from "./pages/CheckoutPage";
import OrderHistoryPage from "./pages/OrderHistoryPage";
import { MOCK_PRODUCTS, type Product, type CartItem } from "./data/productsMock";
import api from "./lib/api";

import "./App.css";
import Login from "./container/auth/Login";
import Dashboard from "./container/seller/Dashboard";
import ProductManagement from "./container/seller/Product";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import OrderFulfillment from "./container/seller/OrderFulfillment";

const App = () => {
  const navigate = useNavigate();
  const [currentPage, setCurrentPage] = useState<"catalog" | "detail" | "login" |"cart"| "checkout" | "history">("catalog");
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false);
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [cartCount, setCartCount] = useState<number>(0);
  const [pendingProductId, setPendingProductId] = useState<string | null>(null);
  const [selectedCheckoutItems, setSelectedCheckoutItems] = useState<any[]>([]);

  // STATE PENCARIAN GLOBAL
  const [catalogSearchQuery, setCatalogSearchQuery] = useState<string>("");

  useEffect(() => {
    fetchCartCount();
  }, []);

  const handleNavigateToCart = () => {
    if (!isLoggedIn) {
      setCurrentPage("login");
    } else {
      setCurrentPage("cart");
    }
  };

  const handleAddToCart = async (productId: string, quantity: number = 1) => {
    const token = localStorage.getItem("token");

    if (!token) {
      setCurrentPage("login"); 
      return;
    }

    try {
      const response = await api.post("/api/v1/carts/items", {
        productId: productId,
        quantity: quantity
      });

      if (response.data && response.data.code === 200) {
        fetchCartCount();
      }
    } catch (error: any) {
      console.error("Gagal menambahkan ke keranjang:", error);
      const errorMessage = error.response?.data?.message || "Terjadi kesalahan pada server.";
      alert(`Gagal: ${errorMessage}`);
    }
  };

  const fetchCartCount = async () => {
    const token = localStorage.getItem("token");
    if (!token) {
      setCartCount(0);
      return;
    }

    try {
      const response = await api.get("/api/v1/carts");
      if (response.data && response.data.code === 200) {
        const items = response.data.data || [];
        const totalQuantity = items.reduce((sum: number, item: any) => sum + item.quantity, 0);
        setCartCount(totalQuantity);
      }
    } catch (error) {
      console.error("Gagal mengambil jumlah keranjang:", error);
    }
  };

  const handleLoginSuccess = () => {
    setIsLoggedIn(true);

    if (pendingProductId) {
      const productData = MOCK_PRODUCTS.find((p) => p.id === pendingProductId);
      
      if (productData) {
        const newItem: CartItem = {
          id: productData.id,
          name: productData.name,
          store: "JatiStore Official",
          price: productData.price,
          image: productData.image,
          variant: "Standard Version",
          quantity: 1,
          maxStock: 5,
        };
        
        setCartItems([newItem]);
      }
      setPendingProductId(null); 
      setCurrentPage("cart");
    } else {
      setCurrentPage("catalog");
    }
  };

  return (
    <div className="text-on-background bg-background min-h-screen flex flex-col font-sans antialiased">
      
      {/* 👑 GLOBAL HEADER UTAMA */}
      {currentPage !== "login" && (
        <nav className="bg-surface border-b border-outline-variant shadow-sm w-full sticky top-0 z-50">
          <div className="flex justify-between items-center w-full px-margin-mobile md:px-margin-desktop max-w-container-max mx-auto h-16 gap-4">
            
            {/* Sebelah Kiri: Logo */}
            <div className="flex items-center flex-shrink-0">
              <button 
                onClick={() => {
                  setCatalogSearchQuery(""); 
                  setCurrentPage("catalog");
                }} 
                className="text-headline-md font-headline-lg font-bold text-primary hover:opacity-80 transition-opacity"
              >
                JatiStore
              </button>
            </div>

      {currentPage === "catalog" && (
  <CatalogPage 
    onProductClick={(productId) => {
      const product = MOCK_PRODUCTS.find((p) => p.id === productId);
      if (product) {
        setSelectedProduct(product);
        setCurrentPage("detail");
      }
    }}
    onCartClick={handleNavigateToCart}
    // Oper fungsi asli ke properti onAddToCart di sini
    onAddToCart={handleAddToCart} // 🌟 PASTIKAN BARIS INI ADA
    isLoggedIn={isLoggedIn}
    onLoginClick={() => navigate("/auth/login")}
    onLogoutClick={() => {
      setIsLoggedIn(false);
      setCartItems([]);
    }}
    />
    )}

            </div>
          </div>
        </nav>
      )}

      {/* RENDER KONTEN HALAMAN */}
      {currentPage === "catalog" && (
        <CatalogPage 
          onProductClick={(productId) => {
            setSelectedProduct(productId);
            setCurrentPage("detail");
          }}
          onCartClick={handleNavigateToCart}
          onAddToCart={handleAddToCart} 
          isLoggedIn={isLoggedIn}
          onLoginClick={() => setCurrentPage("login")}
          onLogoutClick={() => {
            setIsLoggedIn(false);
            setCartItems([]);
          }}
          searchQuery={catalogSearchQuery}
        />
      )}

      {currentPage === "detail" && selectedProduct && (
        <ProductDetailPage 
          productId={selectedProduct} 
          onBackToCatalog={() => setCurrentPage("catalog")} 
          onAddToCart={handleAddToCart}
          onCartClick={handleNavigateToCart} 
        />
      )}

      {currentPage === "login" && (
        <LoginPage 
          onLoginSuccess={handleLoginSuccess} 
          onBackToCatalog={() => {
            setPendingProductId(null);
            setCurrentPage("catalog");
          }}
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
        />
      )}

      {currentPage === "checkout" && (
        <CheckoutPage 
          cartItems={selectedCheckoutItems}
          onBackToCart={() => setCurrentPage("cart")}
          onPaymentSuccess={() => {
            setSelectedCheckoutItems([]);
            fetchCartCount();
            setCurrentPage("history");
          }}
        />
      )}

      {currentPage === "history" && (
        <OrderHistoryPage 
          onNavigateHome={() => setCurrentPage("catalog")}
          onNavigateCart={() => setCurrentPage("cart")}
          cartCount={cartItems.reduce((acc, item) => acc + item.quantity, 0)}
        />
      )}

    </div>
  );
};

import { AdminLayout } from "./components/layout/admin/AdminLayout.tsx";
import { Dashboard as AdminDashboard } from "./container/admin/Dashboard.tsx";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/*" element={<App />} />
        <Route path="/auth/login" element={<Login />} />
        
        {/* Seller Routes */}
        <Route element={<ProtectedRoute allowedRoles={["SELLER"]} />}>
          <Route path="/seller/dashboard" element={<Dashboard />} />
          <Route path="/seller/products" element={<ProductManagement />} />
          <Route path="/seller/orders" element={<OrderFulfillment />} />
        </Route>

        {/* Admin Routes */}
        <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
          <Route element={<AdminLayout />}>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
            {/* Flash Sale and Audit Trails will go here in the future */}
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  </StrictMode>,
);