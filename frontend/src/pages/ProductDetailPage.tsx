import React, { useState, useEffect } from "react";
import api from "../lib/api"; 

// 1. Updated interface to include dynamic store object matching database structure
interface Product {
  id: string;
  name: string;
  description: string;
  image: string;
  price: number;
  stock: number;
  discountTag?: string;     
  originalPrice?: number;   
  rating?: number;          
  reviewsCount?: number;    
  thumbnails?: string[];    
  // Added relation object based on backend/database structure
  store?: {
    id: string;
    storeName: string;
  };
}

interface ProductDetailPageProps {
  productId: string; 
  onBackToCatalog: () => void;
  onAddToCart: (productId: string, quantity: number) => void;
  onCartClick: () => void; 
}

const ProductDetailPage: React.FC<ProductDetailPageProps> = ({ 
  productId, 
  onBackToCatalog, 
  onAddToCart,
  onCartClick 
}) => {
  const [product, setProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState<number>(1);
  const [mainImage, setMainImage] = useState<string>("");
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    setIsLoading(true);
    api.get(`/api/v1/products/${productId}`)
      .then((response) => {
        if (response.data && response.data.code === 200) {
          const fetchedProduct = response.data.data;
          setProduct(fetchedProduct);
          setMainImage(fetchedProduct.image || "https://placehold.co/600x400?text=No+Image");
          setQuantity(1); 
        }
        setIsLoading(false);
      })
      .catch((error) => {
        console.error("Gagal memuat detail produk:", error);
        setIsLoading(false);
      });
  }, [productId]);

  const handleQuantityChange = (type: "add" | "remove") => {
    if (!product) return;
    if (type === "add" && quantity < product.stock) {
      setQuantity((prev) => prev + 1);
    } else if (type === "remove" && quantity > 1) {
      setQuantity((prev) => prev - 1);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-surface">
        <p className="font-label-md text-on-surface-variant">Memuat detail produk dari database...</p>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-surface gap-4">
        <p className="font-label-md text-error">Produk tidak ditemukan atau telah dihapus.</p>
        <button onClick={onBackToCatalog} className="text-primary font-semibold hover:underline">Back to Catalog</button>
      </div>
    );
  }

  const finalProductRating = product.rating ?? 5;
  const finalReviewsCount = product.reviewsCount ?? 24;
  // 🏪 FALLBACK LOGIC: Mengambil storeName dari objek store database jika tersedia
  const finalSoldBy = product.store?.storeName ?? "JatiStore Official";
  const finalThumbnails = product.thumbnails ?? [];

  return (
    <>
      {/* Main Content */}
      <main className="flex-grow w-full max-w-container-max mx-auto px-margin-desktop py-stack-lg flex flex-col gap-stack-lg">
        {/* Breadcrumb / Back Button */}
        <button onClick={onBackToCatalog} className="flex items-center gap-1 text-primary text-body-sm font-semibold hover:underline self-start">
          <span className="material-symbols-outlined text-[18px]">arrow_back</span> Back to Catalog
        </button>

        {/* Product Section */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
          {/* Image Gallery */}
          <div className="lg:col-span-7 flex flex-col gap-stack-sm">
            <div className="w-full aspect-[4/3] rounded-xl overflow-hidden bg-surface-container-lowest border border-outline-variant shadow-sm relative group">
              <img alt={product.name} className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" src={mainImage} />
              {product.discountTag && (
                <div className="absolute top-4 left-4 bg-error text-on-error px-3 py-1 rounded-full font-label-sm text-label-sm uppercase tracking-wide opacity-90 shadow-sm backdrop-blur-sm">
                  {product.discountTag}
                </div>
              )}
            </div>
            
            {/* Thumbnails */}
            <div className="flex gap-stack-sm overflow-x-auto pb-2 snap-x">
              <div 
                onClick={() => setMainImage(product.image || "https://placehold.co/600x400?text=No+Image")}
                className={`w-24 h-24 shrink-0 rounded-lg border-2 overflow-hidden cursor-pointer snap-start transition-all ${mainImage === product.image ? "border-primary opacity-100" : "border-outline-variant opacity-70 hover:opacity-100"}`}
              >
                <img alt="Main image thumbnail" className="w-full h-full object-cover" src={product.image || "https://placehold.co/600x400?text=No+Image"} />
              </div>
              {finalThumbnails.map((thumb, index) => (
                <div 
                  key={index}
                  onClick={() => setMainImage(thumb)}
                  className={`w-24 h-24 shrink-0 rounded-lg border-2 overflow-hidden cursor-pointer snap-start transition-all ${mainImage === thumb ? "border-primary opacity-100" : "border-outline-variant opacity-70 hover:opacity-100"}`}
                >
                  <img alt={`Thumbnail ${index + 1}`} className="w-full h-full object-cover" src={thumb} />
                </div>
              ))}
            </div>
          </div>

          {/* Product Details */}
          <div className="lg:col-span-5 flex flex-col gap-stack-md bg-surface-container-lowest p-gutter rounded-xl shadow-sm border border-outline-variant">
            <div>
              <div className="flex items-center gap-1 mb-1 text-on-surface-variant">
                <span className="material-symbols-outlined text-label-sm" style={{ fontSize: "16px" }}>storefront</span>
                <span className="font-label-sm text-label-sm uppercase tracking-wider">Sold by {finalSoldBy}</span>
              </div>
              <h1 className="text-headline-lg font-headline-lg text-on-surface mb-2">{product.name}</h1>
              <div className="flex items-center gap-2 mb-4">
                <div className="flex text-tertiary-fixed-dim">
                  {Array.from({ length: Math.floor(finalProductRating) }).map((_, i) => (
                    <span key={i} className="material-symbols-outlined text-[20px]" style={{ fontVariationSettings: "'FILL' 1" }}>star</span>
                  ))}
                </div>
                <span className="font-body-sm text-body-sm text-on-surface-variant underline cursor-pointer hover:text-primary transition-colors">({finalReviewsCount} Reviews)</span>
              </div>
            </div>

            <div className="flex items-baseline gap-3 pb-stack-md border-b border-outline-variant">
              <span className="text-display-lg font-display-lg text-error">Rp {product.price.toLocaleString("id-ID")}</span>
              {product.originalPrice && (
                <span className="text-headline-md font-headline-md text-on-surface-variant line-through opacity-70">Rp {product.originalPrice.toLocaleString("id-ID")}</span>
              )}
            </div>

            <p className="font-body-md text-body-md text-on-surface-variant leading-relaxed">
              {product.description || "Tidak ada deskripsi untuk produk ini."}
            </p>

            <div className="flex items-center gap-4 py-2">
              <span className="flex items-center justify-center w-auto px-3 h-8 rounded bg-surface-container-high text-on-surface font-mono-data text-mono-data">Stok: {product.stock}</span>
              <span className="font-label-sm text-label-sm text-on-surface-variant uppercase tracking-wider">In Stock</span>
            </div>

            <div className="mt-auto pt-stack-md flex gap-stack-sm">
              <div className="flex border border-outline-variant rounded-lg overflow-hidden h-12 w-32 bg-surface">
                <button onClick={() => handleQuantityChange("remove")} className="w-10 flex items-center justify-center text-on-surface-variant hover:bg-surface-container transition-colors">
                  <span className="material-symbols-outlined">remove</span>
                </button>
                <input className="w-full text-center border-none bg-transparent font-body-md text-body-md focus:ring-0 p-0" type="text" readOnly value={quantity} />
                <button onClick={() => handleQuantityChange("add")} className="w-10 flex items-center justify-center text-on-surface-variant hover:bg-surface-container transition-colors">
                  <span className="material-symbols-outlined">add</span>
                </button>
              </div>
              
              <button 
                onClick={async () => {
                  await onAddToCart(product.id, quantity);
                  onCartClick(); 
                }}
                className="flex-1 bg-primary hover:bg-primary/90 text-on-primary font-label-md text-label-md py-3 px-6 rounded-full transition-colors flex items-center justify-center gap-2 shadow-sm"
              >
                <span className="material-symbols-outlined">shopping_cart</span>
                Tambah Keranjang
              </button>
            </div>
          </div>
        </div>

        {/* Secondary Section */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-gutter mt-stack-lg">
          <div className="lg:col-span-1 bg-surface-container-low rounded-xl p-gutter border border-outline-variant">
            <h3 className="text-headline-md font-headline-md text-on-surface mb-stack-md flex items-center gap-2">
              <span className="material-symbols-outlined text-[20px] text-primary">forum</span> Reviews
            </h3>
            <div className="flex items-center gap-4 mb-6">
              <div className="text-display-lg font-display-lg text-on-surface">{finalProductRating.toFixed(1)}</div>
              <div className="flex flex-col">
                <div className="flex text-tertiary-fixed-dim">
                  {Array.from({ length: Math.floor(finalProductRating) }).map((_, i) => (
                    <span key={i} className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>star</span>
                  ))}
                </div>
                <span className="font-label-sm text-label-sm text-on-surface-variant">Based on {finalReviewsCount} reviews</span>
              </div>
            </div>
            <button className="w-full py-2 border border-primary text-primary hover:bg-primary hover:text-on-primary rounded-lg font-label-md text-label-md transition-colors">Write a Review</button>
          </div>

          <div className="lg:col-span-2 bg-surface-container-lowest rounded-xl p-gutter border border-outline-variant shadow-sm">
            <h3 className="text-headline-md font-headline-md text-on-surface mb-stack-md flex items-center gap-2">
              <span className="material-symbols-outlined text-[20px] text-primary">interests</span> Related Products
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-stack-md">
              <div className="group cursor-pointer">
                <div className="aspect-square rounded-lg overflow-hidden bg-surface-container mb-2 relative">
                  <img alt="Steel Bottle" className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDABg9qwjJl_K4ehHWRvKqVCspG_paSEXXqJJOMzgM7tj_72VisNit0zdRVayrWukirhTQqGOwqFDyEpE7jYVaD3_m1lenI6Rzd2HIgJnrfsU49cdAthjMgNm1FhOuBKdmHkQANxvXLo_m3ZgD9ioZNq1Fxz6tAkqQrrRF2d-GAbiNRZFxPoch5rFILfb9gICbbM1XcpNgYUPwnq83cVyMklFqzJTfKPvkdExNoeqNCmrlBv6W4LuVcy5HGpYbLWJHDnaw2AZk7qxrS" />
                </div>
                <p className="text-label-sm font-label-sm text-on-surface-variant/70 mb-1">Eco Gear</p>
                <h4 className="font-label-md text-label-md text-on-surface truncate group-hover:text-primary transition-colors">Reusable Steel Bottle</h4>
                <p className="font-body-sm text-body-sm text-on-surface-variant">Rp 350.000</p>
              </div>
              <div className="group cursor-pointer">
                <div className="aspect-square rounded-lg overflow-hidden bg-surface-container mb-2 relative">
                  <img alt="Cotton Bags" className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" src="https://lh3.googleusercontent.com/aida-public/AB6AXuATr8f6Ui4Nq-HHwI1JP-Dn_6BpJRQFOHM9c1xMHHB_meQ3__PhqXH07u6WZFHD3aPBDuuZ6KlesCC0F7L5YExq0rzxtWOUrJPyg6dT9nXx-3wJ2gXo_o6sxyRySsLxnRAcdOpVcviGeAUVtzHVhBXQeEMdixF-B0nqOcoM5gnjFDywvT0EDmiVSXAQEkQ7qqbhUpbRYPNTw978r1F9CVp6lb9TJ8byW5QbyrqtLVa_PuylT6_MY6yxUfIqHPyVra3UkHu7LIGe7eJk" />
                </div>
                <p className="text-label-sm font-label-sm text-on-surface-variant/70 mb-1">Green Living</p>
                <h4 className="font-label-md text-label-md text-on-surface truncate group-hover:text-primary transition-colors">Organic Produce Bags</h4>
                <p className="font-body-sm text-body-sm text-on-surface-variant">Rp 120.000</p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </>
  );
};

export default ProductDetailPage;