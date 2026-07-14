import { StrictMode, useState } from "react";
import { createRoot } from "react-dom/client";
import CatalogPage from "./pages/CatalogPage";
import ProductDetailPage from "./pages/ProductDetailPage";
import LoginPage from "./pages/LoginPage";
import CartPage from "./pages/CartPage";
import CheckoutPage from "./pages/CheckoutPage";
import OrderHistoryPage from "./pages/OrderHistoryPage";
import { MOCK_PRODUCTS, type Product, type CartItem } from "./data/productsMock";

const App = () => {
  const [currentPage, setCurrentPage] = useState<"catalog" | "detail" | "login" |"cart"| "checkout" | "history">("catalog");
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false);
  const [cartItems, setCartItems] = useState<CartItem[]>([]);

  const [pendingProductId, setPendingProductId] = useState<string | null>(null);

  const handleNavigateToCart = () => {
    if (!isLoggedIn) {
      setCurrentPage("login");
    } else {
      setCurrentPage("cart");
    }
  };

//   const handleAddToCart = (productId: string) => {
//     if (!isLoggedIn) {
//       setPendingProductId(productId); 
//       setCurrentPage("login");
//       return;
//     }

//     const productData = MOCK_PRODUCTS.find((p) => p.id === productId);
//     if (!productData) return;

//     setCartItems((prevItems) => {
//       const isExist = prevItems.find((item) => item.id === productId);

//       if (isExist) {
//         if (isExist.quantity < 5) {
//           return prevItems.map((item) =>
//             item.id === productId ? { ...item, quantity: item.quantity + 1 } : item
//           );
//         }
//         alert("Stok maksimal untuk produk ini sudah tercapai di keranjang!");
//         return prevItems;
//       }

//       const newItem: CartItem = {
//         id: productData.id,
//         name: productData.name,
//         store: "JatiStore Official",
//         price: productData.price,
//         image: productData.image,
//         variant: "Standard Version",
//         quantity: 1,
//         maxStock: 5,
//       };
      
//       return [...prevItems, newItem];
//     });
//   };

  const handleAddToCart = (productId: string) => {
  const productData = MOCK_PRODUCTS.find((p) => p.id === productId);
  if (!productData) return;

  setCartItems((prevItems) => {
    const isExist = prevItems.find((item) => item.id === productId);

    if (isExist) {
      if (isExist.quantity < 5) {
        alert(`${productData.name} ditambahkan lagi ke keranjang!`);
        return prevItems.map((item) =>
          item.id === productId ? { ...item, quantity: item.quantity + 1 } : item
        );
      }
      alert("Stok maksimal untuk produk ini sudah tercapai di keranjang!");
      return prevItems;
    }

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
    setCurrentPage("cart");
    
    // alert(`${productData.name} berhasil dimasukkan ke keranjang!`);
    return [...prevItems, newItem];
  });
};

//   const handleLoginSuccess = () => {
//     setIsLoggedIn(true);

//     if (pendingProductId) {
//       const productData = MOCK_PRODUCTS.find((p) => p.id === pendingProductId);
      
//       if (productData) {
//         const newItem: CartItem = {
//           id: productData.id,
//           name: productData.name,
//           store: "JatiStore Official",
//           price: productData.price,
//           image: productData.image,
//           variant: "Standard Version",
//           quantity: 1,
//           maxStock: 5,
//         };
        
//         setCartItems([newItem]);
//       }
//       setPendingProductId(null); 
//     }
//     setCurrentPage("cart");
//   };

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
      {/* {currentPage === "catalog" && (
        <CatalogPage 
          onProductClick={(productId) => {
            const product = MOCK_PRODUCTS.find((p) => p.id === productId);
            if (product) {
              setSelectedProduct(product);
              setCurrentPage("detail");
            }
          }}
          onCartClick={handleNavigateToCart}
          onAddToCart={handleAddToCart} 
        />
      )} */}

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
    onLoginClick={() => setCurrentPage("login")}
    onLogoutClick={() => {
      setIsLoggedIn(false);
      setCartItems([]);
    }}
    />
    )}

    {currentPage === "detail" && selectedProduct && (
    <ProductDetailPage 
        product={selectedProduct} 
        onBackToCatalog={() => setCurrentPage("catalog")} 
        // Oper fungsi asli ke properti onAddToCart di sini
        onAddToCart={handleAddToCart} // 🌟 PASTIKAN BARIS INI ADA
    />
    )}

      {/* {currentPage === "catalog" && (
        <CatalogPage 
          onProductClick={(productId) => {
            const product = MOCK_PRODUCTS.find((p) => p.id === productId);
            if (product) {
              setSelectedProduct(product);
              setCurrentPage("detail");
            }
          }}
          onCartClick={handleNavigateToCart}
          onAddToCart={handleAddToCart} 
          isLoggedIn={isLoggedIn}
          onLoginClick={() => setCurrentPage("login")}
          onLogoutClick={() => {
            setIsLoggedIn(false);
            setCartItems([]);
          }}
        />
      )}


      {currentPage === "detail" && selectedProduct && (
        <ProductDetailPage 
          product={selectedProduct} 
          onBackToCatalog={() => setCurrentPage("catalog")} 
          onAddToCart={handleAddToCart}
        />
      )} */}

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
          cartItems={cartItems}
          setCartItems={setCartItems}
          onBackToCatalog={() => setCurrentPage("catalog")}
          onCheckout={() => setCurrentPage("checkout")}
          onLogout={() => {
            setIsLoggedIn(false);
            setCartItems([]);
            setCurrentPage("catalog");
          }}
        />
      )}

      {currentPage === "checkout" && (
        <CheckoutPage 
          cartItems={cartItems}
          onBackToCart={() => setCurrentPage("cart")}
          onPaymentSuccess={() => {
            setCartItems([]);
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

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
