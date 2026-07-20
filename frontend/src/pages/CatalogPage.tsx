import React, { useState, useEffect } from "react";
import api from "../lib/api";

interface Product {
  id: string;
  name: string;
  storeName: string;
  description: string;
  image: string;
  price: number;
  originalPrice?: number;
  stock: number;
  isFlashSale?: boolean;
  flashSaleEndTime?: string;
  category?: {
    id: string;
    name: string;
  };
  categoryId?: string;
}

interface CatalogPageProps {
  onProductClick: (id: string) => void;
  onCartClick: () => void;
  onAddToCart: (id: string, quantity: number) => void; 
  isLoggedIn: boolean;
  onLoginClick: () => void;
  onLogoutClick: () => void;
  searchQuery: string; // 💡 Menerima kata kunci pencarian dari Header Global main.tsx
}

interface Category {
  id: string;
  name: string;
  icon: string;
}

const CatalogPage: React.FC<CatalogPageProps> = ({
  onProductClick,
  onCartClick,
  onAddToCart,
  isLoggedIn,
  onLoginClick,
  onLogoutClick,
  searchQuery // 💡 Ambil properti pencarian
}) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [cartCount, setCartCount] = useState<number>(0);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState<string | null>(null);
  const [minPrice, setMinPrice] = useState<number>(0);
  const [maxPrice, setMaxPrice] = useState<number>(100000000);
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [pageSize] = useState<number>(20);

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
      console.error("Failed to load cart count:", error);
    }
  };

  const fetchProducts = async (page: number) => {
    setIsLoading(true);
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: pageSize.toString()
      });

      if (searchQuery) {
        params.append('search', searchQuery);
      }
      
      if (selectedCategoryId) {
        params.append('categoryId', selectedCategoryId);
      }

      const response = await api.get(`/api/v1/products?${params.toString()}`);
      if (response.data && response.data.code === 200) {
        const data = response.data.data;
        setProducts(data.content || []);
        setTotalPages(data.totalPages || 0);
      }
    } catch (error) {
      console.error("Failed to load products:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    setCurrentPage(0);
  }, [searchQuery, selectedCategoryId]);

  useEffect(() => {
    fetchProducts(currentPage);
  }, [currentPage, searchQuery, selectedCategoryId]);

  useEffect(() => {
    api.get("/api/v1/public/categories")
      .then((res) => {
        if (res.data?.code === 200) setCategories(res.data.data);
      })
      .catch((err) => console.error("Failed to load categories:", err));

    fetchCartCount();
  }, []);

  const filteredProducts = products.filter((product) => {
    const matchesPrice = product.price >= minPrice && product.price <= maxPrice;
    return matchesPrice;
  });

  return (
    <>
      {/* 💡 HEADER LAMA SUDAH DIHAPUS BERSIH KARENA SUDAH MEMAKAI HEADER GLOBAL DI main.tsx */}

      {/* Main Content */}
      <main className="w-full max-w-container-max mx-auto px-margin-mobile md:px-margin-desktop py-stack-lg flex flex-col gap-stack-lg">
        
        {/* Hero Banner */}
        <section className="w-full rounded-xl overflow-hidden shadow-sm relative group cursor-pointer h-48 md:h-64 lg:h-[320px]">
          <div className="bg-cover bg-center w-full h-full absolute inset-0" style={{ backgroundImage: 'url("https://lh3.googleusercontent.com/aida-public/AB6AXuApbnHv6bPnGLVj4F3I6s4zDl3ut-7ToyWRVdMAgosuZjCt9fwLpO820HDDMYUPysEpTMO5p-OtMAeJm7qVuO99LP6r-Uykmrqbmoh54mYavqdrW6jsPbORPvwwNuUa0YvuzNLK_ru-8mDAncBvuXaG0jd97nZJZfuLHvo3d1nxXTfm5-QRRDANun8bbPnZ9THC5Y9GqFN5a3UmUc8aQIRpEsL8AWhL6ZpA4uXY5TWBFGqW04m29ADOWRiETPB4PyOFKABFB0968ltKY6o")' }}></div>
          <div className="absolute inset-0 bg-gradient-to-r from-surface-inverse/80 to-transparent flex items-center p-8 md:p-12">
            <div className="max-w-md">
              <h2 className="font-display-lg text-display-lg text-on-primary mb-2 leading-tight">Mid-Year Discount Event</h2>
              <h3 className="font-headline-lg text-headline-lg text-on-primary/90 mb-4">TECH & STYLE FESTIVAL</h3>
              <p className="font-body-lg text-body-lg text-on-primary/90 mb-6">Up to 70% off for all electronics and fashion categories.</p>
              <button className="bg-surface text-primary font-label-md text-label-md px-6 py-3 rounded-full hover:bg-primary-fixed transition-colors shadow-sm">Shop Now</button>
            </div>
          </div>
        </section>

        {/* Sidebar + Content Layout */}
        <div className="flex gap-stack-lg">

          {/* Left Sidebar - Category Filter */}
          <aside className="w-60 flex-shrink-0 hidden md:block">
            <div className="sticky top-20 bg-surface rounded-lg border border-outline-variant p-4 space-y-6">
              <div>
                <h3 className="font-headline-sm text-headline-sm text-on-surface mb-4">Categories</h3>

                <div className="flex flex-col gap-2">
                  <button
                    onClick={() => setSelectedCategoryId(null)}
                    className={`flex items-center gap-3 p-3 rounded-lg text-left transition-colors ${
                      selectedCategoryId === null
                        ? 'bg-primary text-on-primary'
                        : 'hover:bg-surface-container text-on-surface'
                    }`}
                  >
                    <span className="material-symbols-outlined">apps</span>
                    <span className="font-label-md">All</span>
                  </button>

                  {categories.map((cat) => (
                    <button
                      key={cat.id}
                      onClick={() => setSelectedCategoryId(cat.id)}
                      className={`flex items-center gap-3 p-3 rounded-lg text-left transition-colors ${
                        selectedCategoryId === cat.id
                          ? 'bg-primary-container text-on-primary-container border-l-4 border-primary'
                          : 'hover:bg-surface-container text-on-surface'
                      }`}
                    >
                      <span className="material-symbols-outlined">{cat.icon}</span>
                      <span className="font-label-md">{cat.name}</span>
                    </button>
                  ))}
                </div>
              </div>

              <div className="pt-4 border-t border-outline-variant">
                <h3 className="font-headline-sm text-headline-sm text-on-surface mb-4">Price</h3>

                <div className="space-y-3">
                  <div>
                    <label className="block font-label-sm text-label-sm text-on-surface-variant mb-1">Min</label>
                    <input
                      type="number"
                      min="0"
                      value={minPrice === 0 ? '' : minPrice}
                      onChange={(e) => {
                        const val = e.target.value;
                        if (val === '') {
                          setMinPrice(0);
                        } else {
                          const num = parseInt(val, 10);
                          if (!isNaN(num) && num >= 0) {
                            setMinPrice(num);
                          }
                        }
                      }}
                      className="w-full border border-outline-variant rounded-lg h-10 px-3 text-body-sm font-body-sm bg-surface text-on-surface focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
                      placeholder="0"
                    />
                  </div>

                  <div>
                    <label className="block font-label-sm text-label-sm text-on-surface-variant mb-1">Max</label>
                    <input
                      type="number"
                      min="0"
                      value={maxPrice === 100000000 ? '' : maxPrice}
                      onChange={(e) => {
                        const val = e.target.value;
                        if (val === '') {
                          setMaxPrice(100000000);
                        } else {
                          const num = parseInt(val, 10);
                          if (!isNaN(num) && num >= 0) {
                            setMaxPrice(num);
                          }
                        }
                      }}
                      className="w-full border border-outline-variant rounded-lg h-10 px-3 text-body-sm font-body-sm bg-surface text-on-surface focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
                      placeholder="100000000"
                    />
                  </div>

                  <button
                    onClick={() => {
                      setMinPrice(0);
                      setMaxPrice(100000000);
                    }}
                    className="w-full py-2 text-label-sm font-label-sm text-primary hover:bg-primary-container/10 rounded-lg transition-colors"
                  >
                    Reset Price
                  </button>
                </div>
              </div>
            </div>
          </aside>

          {/* Main Content - Products */}
          <section className="flex-1 min-w-0">
            <div className="flex justify-between items-center mb-stack-md">
              <h3 className="font-headline-md text-headline-md text-on-surface">Best Selling Products</h3>
            </div>

            {isLoading ? (
              <div className="text-center py-10 font-label-md text-on-surface-variant">Loading products from database...</div>
            ) : filteredProducts.length === 0 ? (
              <div className="text-center py-12 bg-surface-container-lowest border border-outline-variant rounded-lg">
                <span className="material-symbols-outlined text-[48px] text-on-surface-variant/40">search_off</span>
                <p className="text-body-lg font-body-lg text-on-surface-variant mt-2">Product "{searchQuery}" not found</p>
              </div>
            ) : (
              <>
                <div className="grid grid-cols-2 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-stack-sm md:gap-stack-md">
                  {filteredProducts.map((product) => (
                    <div
                      key={product.id}
                      onClick={() => onProductClick(product.id)}
                      className="bg-surface rounded-lg border border-outline-variant overflow-hidden shadow-sm hover:shadow-md transition-shadow group flex flex-col h-full cursor-pointer"
                    >
                      <div className="h-48 w-full bg-surface-container relative overflow-hidden">
                        <img
                          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                          src={product.image || "https://placehold.co/600x400?text=No+Image"}
                          alt={product.name}
                        />
                      </div>
                      <div className="p-3 flex flex-col flex-1">
                        <span className="text-label-sm font-label-sm text-on-surface-variant mb-1 block">
                          {product.storeName || "Unknown Store"}
                        </span>
                        <h4 className="font-body-sm text-body-sm text-on-surface line-clamp-2 mb-2 flex-1">{product.name}</h4>
                        <span className="font-label-md text-label-md text-primary mb-3 block">Rp {product.price.toLocaleString("id-ID")}</span>

                        <button
                            onClick={async (e) => {
                              e.stopPropagation();
                              await onAddToCart(product.id, 1);
                              await fetchCartCount();
                              onCartClick();
                            }}
                            className="w-full py-2 bg-surface border border-primary text-primary font-label-sm text-label-sm rounded-md hover:bg-primary-container/10 transition-colors mt-auto"
                          >
                            Add to Cart
                        </button>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Pagination Controls */}
                {totalPages > 1 && (
                  <div className="flex justify-center items-center gap-2 mt-8">
                    <button
                      onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                      disabled={currentPage === 0}
                      className="px-4 py-2 border border-outline-variant rounded-lg font-label-sm text-label-sm text-on-surface disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface-container transition-colors"
                    >
                      Previous
                    </button>

                    <div className="flex gap-2">
                      {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                        let pageNum = i;
                        if (totalPages > 5) {
                          if (currentPage < 3) {
                            pageNum = i;
                          } else if (currentPage > totalPages - 3) {
                            pageNum = totalPages - 5 + i;
                          } else {
                            pageNum = currentPage - 2 + i;
                          }
                        }
                        return (
                          <button
                            key={pageNum}
                            onClick={() => setCurrentPage(pageNum)}
                            className={`w-10 h-10 rounded-lg font-label-sm text-label-sm transition-colors ${
                              currentPage === pageNum
                                ? 'bg-primary text-on-primary'
                                : 'border border-outline-variant text-on-surface hover:bg-surface-container'
                            }`}
                          >
                            {pageNum + 1}
                          </button>
                        );
                      })}
                    </div>

                    <button
                      onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                      disabled={currentPage === totalPages - 1}
                      className="px-4 py-2 border border-outline-variant rounded-lg font-label-sm text-label-sm text-on-surface disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface-container transition-colors"
                    >
                      Next
                    </button>
                  </div>
                )}
              </>
            )}
          </section>
        </div>
      </main>
    </>
  );
};

export default CatalogPage;