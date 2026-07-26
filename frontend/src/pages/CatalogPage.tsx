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
  onProductClick: (id: string, isFlashSale?: boolean) => void;
  onCartClick: () => void;
  onAddToCart: (id: string, quantity: number) => void; 
  searchQuery: string;
  onCheckout: (checkedItems: any[]) => void;
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
  searchQuery: initialSearchQuery,
  onCheckout
}) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState<string | null>(null);
  
  const [localSearchInput, setLocalSearchInput] = useState<string>(initialSearchQuery || "");
  const [appliedSearchQuery, setAppliedSearchQuery] = useState<string>(initialSearchQuery || "");

  const [minPrice] = useState<number>(0);
  const [maxPrice] = useState<number>(100000000);
  
  // Pagination State
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [totalElements, setTotalElements] = useState<number>(0);
  const [pageSize] = useState<number>(20);
  const [isFlashSaleOnly, setIsFlashSaleOnly] = useState<boolean>(false);
  
  // Dynamic State for Flash Sale from Backend
  const [flashSaleEvent, setFlashSaleEvent] = useState<{ name: string; endTime: string } | null>(null);
  const [upcomingFlashSaleEvent, setUpcomingFlashSaleEvent] = useState<{ name: string; startTime: string } | null>(null);
  const [timeLeft, setTimeLeft] = useState({ days: 0, hours: 0, minutes: 0, seconds: 0 });
  const [serverTimeOffset, setServerTimeOffset] = useState<number>(0);

  // 1. Fetch active and upcoming flash sale data from backend
  const checkFlashSaleStatus = async () => {
    try {
      const [activeRes, upcomingRes] = await Promise.all([
        api.get("/api/v1/public/flash-sale/active").catch(() => null),
        api.get("/api/v1/public/flash-sale/upcoming").catch(() => null)
      ]);
      
      let hasActive = false;
      let timestampStr = null;

      if (activeRes?.data && (activeRes.data.code === 200 || activeRes.data.restApiResponseHttpCode === 200)) {
        timestampStr = activeRes.data.timestamp || activeRes.data.restApiResponseTimestamp;
        const activeData = activeRes.data.data || activeRes.data.restApiResponseData;
        if (activeData && activeData.endTime) {
          setFlashSaleEvent({
            name: activeData.name || "Flash Sale",
            endTime: activeData.endTime
          });
          setUpcomingFlashSaleEvent(null);
          hasActive = true;
        } else {
          setFlashSaleEvent(null);
          setIsFlashSaleOnly(false);
        }
      } else {
        setFlashSaleEvent(null);
        setIsFlashSaleOnly(false);
      }

      if (!hasActive && upcomingRes?.data && (upcomingRes.data.code === 200 || upcomingRes.data.restApiResponseHttpCode === 200)) {
        if (!timestampStr) timestampStr = upcomingRes.data.timestamp || upcomingRes.data.restApiResponseTimestamp;
        const upcomingData = upcomingRes.data.data || upcomingRes.data.restApiResponseData;
        if (upcomingData && upcomingData.startTime) {
          setUpcomingFlashSaleEvent({
            name: upcomingData.name || "Upcoming Flash Sale",
            startTime: upcomingData.startTime
          });
        } else {
          setUpcomingFlashSaleEvent(null);
        }
      } else {
        setUpcomingFlashSaleEvent(null);
      }

      if (timestampStr) {
        const serverTime = new Date(timestampStr).getTime();
        const localTime = new Date().getTime();
        setServerTimeOffset(serverTime - localTime);
      }
    } catch (error) {
      console.error("Failed to load flash sale status:", error);
    }
  };

  useEffect(() => {
    checkFlashSaleStatus();
  }, []);

  // 2. Real-time countdown calculation & auto-refresh
  useEffect(() => {
    let targetTime = 0;
    
    if (flashSaleEvent?.endTime) {
      targetTime = new Date(flashSaleEvent.endTime).getTime();
    } else if (upcomingFlashSaleEvent?.startTime) {
      targetTime = new Date(upcomingFlashSaleEvent.startTime).getTime();
    } else {
      return;
    }

    const timer = setInterval(() => {
      const now = new Date().getTime() + serverTimeOffset;
      const difference = targetTime - now;

      if (difference <= 0) {
        clearInterval(timer);
        setTimeLeft({ days: 0, hours: 0, minutes: 0, seconds: 0 });
        // Auto-refresh: Re-check flash sale status (will update state and trigger re-render)
        checkFlashSaleStatus();
      } else {
        const days = Math.floor(difference / (1000 * 60 * 60 * 24));
        const hours = Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((difference % (1000 * 60)) / 1000);
        setTimeLeft({ days, hours, minutes, seconds });
      }
    }, 1000);

    return () => clearInterval(timer);
  }, [flashSaleEvent, upcomingFlashSaleEvent, serverTimeOffset]);


  const fetchProducts = async (page: number) => {
    setIsLoading(true);
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: pageSize.toString()
      });

      if (appliedSearchQuery) {
        params.append('search', appliedSearchQuery);
      }
      
      if (selectedCategoryId) {
        params.append('categoryId', selectedCategoryId);
      }

      // Only send flash sale parameter if the event is active
      if (isFlashSaleOnly && flashSaleEvent) {
        params.append('isFlashSale', 'true');
      }

      const response = await api.get(`/api/v1/products?${params.toString()}`);
      if (response.data && response.data.code === 200) {
        const data = response.data.data;
        setProducts(data.content || []);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      }
    } catch (error) {
      console.error("Failed to load products:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleExecuteSearch = () => {
    setCurrentPage(0);
    setAppliedSearchQuery(localSearchInput);
  };

  useEffect(() => {
    setCurrentPage(0);
  }, [appliedSearchQuery, selectedCategoryId, isFlashSaleOnly]);

  useEffect(() => {
    fetchProducts(currentPage);
  }, [currentPage, appliedSearchQuery, selectedCategoryId, isFlashSaleOnly, flashSaleEvent]);

  useEffect(() => {
    api.get("/api/v1/public/categories")
      .then((res) => {
        if (res.data?.code === 200) setCategories(res.data.data);
      })
      .catch((err) => console.error("Failed to load categories:", err));

  }, []);

  const filteredProducts = products.filter((product) => {
    const matchesPrice = product.price >= minPrice && product.price <= maxPrice;
    // If flash sale event has ended, force product flash sale status to false
    const activeFlashSaleStatus = flashSaleEvent ? product.isFlashSale : false;
    const matchesFlashSale = isFlashSaleOnly ? activeFlashSaleStatus === true : true;
    return matchesPrice && matchesFlashSale;
  });

  const getVisiblePageNumbers = () => {
    const maxVisible = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxVisible / 2));
    let endPage = startPage + maxVisible;

    if (endPage > totalPages) {
      endPage = totalPages;
      startPage = Math.max(0, endPage - maxVisible);
    }

    const pages = [];
    for (let i = startPage; i < endPage; i++) {
      pages.push(i);
    }
    return pages;
  };

  return (
    <>
      <main className="w-full max-w-container-max mx-auto px-margin-mobile md:px-margin-desktop py-stack-lg flex flex-col gap-stack-lg">
        
        {/* Search Bar dengan Tombol Eksplisit */}
        <section className="bg-surface rounded-xl p-4 border border-outline-variant shadow-sm flex flex-col sm:flex-row gap-3 items-center">
          <div className="relative w-full">
            <span className="material-symbols-outlined absolute left-3 top-1/4 -translate-y-1/1 text-on-surface-variant">search</span>
            <input 
              type="text"
              value={localSearchInput}
              onChange={(e) => setLocalSearchInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  handleExecuteSearch();
                }
              }}
              placeholder="Find your needs..."
              className="w-full pl-10 pr-4 py-2.5 bg-surface-container rounded-lg border border-outline-variant focus:border-primary focus:outline-none text-on-surface font-body-sm"
            />
          </div>
          <button 
            onClick={handleExecuteSearch}
            className="w-full sm:w-auto px-6 py-2.5 bg-primary text-on-primary font-label-md font-bold rounded-lg hover:bg-primary/90 transition-colors shadow-sm whitespace-nowrap flex items-center justify-center gap-2"
          >
            <span className="material-symbols-outlined text-[18px]">search</span>
            Search
          </button>
        </section>

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

        {/* Flash Sale Bar - Hanya muncul jika flashSaleEvent aktif dan waktu belum habis */}
        {flashSaleEvent && (
          <section className="bg-gradient-to-r from-error/10 via-error/5 to-surface border border-error/20 rounded-2xl p-6 shadow-sm">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
              <div className="flex items-center gap-3">
                <div className="bg-error text-on-error p-2 rounded-xl flex items-center justify-center shadow-sm">
                  <span className="material-symbols-outlined text-[24px]">bolt</span>
                </div>
                <div>
                  <h3 className="font-headline-lg text-headline-lg text-on-surface flex items-center gap-2">
                    {flashSaleEvent.name} <span className="text-xs bg-error text-on-error px-2 py-0.5 rounded-full uppercase tracking-wider font-bold">HOT</span>
                  </h3>
                  <p className="text-body-sm text-on-surface-variant">Limited time offer, grab your favorite products now!</p>
                </div>
              </div>

              <div className="flex items-center gap-3 flex-wrap">
                <div className="flex items-center gap-2 bg-surface px-4 py-2 rounded-xl border border-outline-variant shadow-sm">
                  <span className="text-xs text-on-surface-variant font-medium">Ends in :</span>
                  <div className="flex items-center gap-1 font-mono font-bold text-error">
                    <span className="bg-error/10 px-2 py-1 rounded">{String(timeLeft.days).padStart(2, '0')}</span>:
                    <span className="bg-error/10 px-2 py-1 rounded">{String(timeLeft.hours).padStart(2, '0')}</span>:
                    <span className="bg-error/10 px-2 py-1 rounded">{String(timeLeft.minutes).padStart(2, '0')}</span>:
                    <span className="bg-error/10 px-2 py-1 rounded">{String(timeLeft.seconds).padStart(2, '0')}</span>
                  </div>
                </div>

                <button
                  onClick={() => {
                    setIsFlashSaleOnly(true);
                    setSelectedCategoryId(null);
                  }}
                  className="bg-error text-on-error px-4 py-2 rounded-xl text-label-md font-label-md font-bold hover:bg-error/90 transition-colors shadow-sm flex items-center gap-1"
                >
                  View All <span className="material-symbols-outlined text-[16px]">arrow_forward</span>
                </button>
              </div>
            </div>
          </section>
        )}

        {/* Upcoming Flash Sale Bar - Akan otomatis muncul jika kurang dari 1 jam */}
        {(() => {
          const isWithinOneHour = timeLeft.days === 0 && (timeLeft.hours === 0 || (timeLeft.hours === 1 && timeLeft.minutes === 0 && timeLeft.seconds === 0));
          return !flashSaleEvent && upcomingFlashSaleEvent && isWithinOneHour && (
            <section className="bg-gradient-to-r from-secondary/10 via-secondary/5 to-surface border border-secondary/20 rounded-2xl p-6 shadow-sm">
              <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
                <div className="flex items-center gap-3">
                  <div className="bg-secondary text-on-secondary p-2 rounded-xl flex items-center justify-center shadow-sm">
                    <span className="material-symbols-outlined text-[24px]">schedule</span>
                  </div>
                  <div>
                    <h3 className="font-headline-lg text-headline-lg text-on-surface flex items-center gap-2">
                      {upcomingFlashSaleEvent.name} <span className="text-xs bg-secondary text-on-secondary px-2 py-0.5 rounded-full uppercase tracking-wider font-bold">SOON</span>
                    </h3>
                    <p className="text-body-sm text-on-surface-variant">Get ready, massive discounts are about to begin!</p>
                  </div>
                </div>

                <div className="flex items-center gap-3 flex-wrap">
                  <div className="flex items-center gap-2 bg-surface px-4 py-2 rounded-xl border border-outline-variant shadow-sm">
                    <span className="text-xs text-on-surface-variant font-medium">Starts in :</span>
                    <div className="flex items-center gap-1 font-mono font-bold text-secondary">
                      <span className="bg-secondary-container px-2 py-1 rounded">{String(timeLeft.days).padStart(2, '0')}</span>:
                      <span className="bg-secondary-container px-2 py-1 rounded">{String(timeLeft.hours).padStart(2, '0')}</span>:
                      <span className="bg-secondary-container px-2 py-1 rounded">{String(timeLeft.minutes).padStart(2, '0')}</span>:
                      <span className="bg-secondary-container px-2 py-1 rounded">{String(timeLeft.seconds).padStart(2, '0')}</span>
                    </div>
                  </div>
                </div>
              </div>
            </section>
          );
        })()}

        {/* Sidebar & Products Layout */}
        <div className="flex flex-col lg:flex-row gap-8 items-start">
          {/* Left Sidebar */}
          <aside className="w-60 flex-shrink-0 hidden md:block">
            <div className="sticky top-20 bg-surface rounded-lg border border-outline-variant p-4 space-y-6">
              <div>
                <h3 className="font-headline-sm text-headline-sm text-on-surface mb-4">Categories</h3>

                <div className="flex flex-col gap-2">
                  <button
                    onClick={() => { setSelectedCategoryId(null); setIsFlashSaleOnly(false); }}
                    className={`flex items-center gap-3 p-3 rounded-lg text-left transition-colors ${
                      selectedCategoryId === null && !isFlashSaleOnly
                        ? 'bg-primary text-on-primary'
                        : 'hover:bg-surface-container text-on-surface'
                    }`}
                  >
                    <span className="material-symbols-outlined">apps</span>
                    <span className="font-label-md">All Products</span>
                  </button>

                  {/* Tombol Flash Sale Only hanya aktif jika flash sale masih berlangsung */}
                  {flashSaleEvent && (
                    <button
                      onClick={() => { setIsFlashSaleOnly(true); setSelectedCategoryId(null); }}
                      className={`flex items-center gap-3 p-3 rounded-lg text-left transition-colors ${
                        isFlashSaleOnly
                          ? 'bg-error text-on-error'
                          : 'hover:bg-surface-container text-error'
                      }`}
                    >
                      <span className="material-symbols-outlined">bolt</span>
                      <span className="font-label-md font-bold">Flash Sale Only</span>
                    </button>
                  )}

                  {categories.map((cat) => (
                    <button
                      key={cat.id}
                      onClick={() => { setSelectedCategoryId(cat.id); setIsFlashSaleOnly(false); }}
                      className={`flex items-center gap-3 p-3 rounded-lg text-left transition-colors ${
                        selectedCategoryId === cat.id && !isFlashSaleOnly
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
            </div>
          </aside>

          {/* Main Content */}
          <section className="flex-1 min-w-0 flex flex-col gap-6">
            <div className="flex justify-between items-center">
              <h3 className="font-headline-md text-headline-md text-on-surface">
                {isFlashSaleOnly ? "Flash Sale Product" : "All Product"}
              </h3>
            </div>

            {isLoading ? (
              <div className="text-center py-16 font-label-md text-on-surface-variant">Loading products from server...</div>
            ) : filteredProducts.length === 0 ? (
              <div className="text-center py-16 bg-surface-container-lowest border border-outline-variant rounded-lg">
                <span className="material-symbols-outlined text-[48px] text-on-surface-variant/40">search_off</span>
                <p className="text-body-lg font-body-lg text-on-surface-variant mt-2">Products not found</p>
              </div>
            ) : (
              <>
                <div className="grid grid-cols-2 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-stack-sm md:gap-stack-md">
                  {filteredProducts.map((product) => {
                    // Discount status is true only if flash sale event is active AND the product is marked for flash sale
                    const isItemFlashSale = flashSaleEvent ? (isFlashSaleOnly || product.isFlashSale) : false;

                    return (
                      <div
                        key={product.id}
                        onClick={() => onProductClick(product.id, isItemFlashSale)}
                        className="bg-surface rounded-lg border border-outline-variant overflow-hidden shadow-sm hover:shadow-md transition-shadow group flex flex-col h-full cursor-pointer relative"
                      >
                        {isItemFlashSale && (
                          <div className="absolute top-2 left-2 z-10 bg-error text-on-error text-[10px] font-bold px-2 py-1 rounded-md shadow">
                            Discount
                          </div>
                        )}

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
                          
                          <div className="mb-3">
                            <div className="flex items-center gap-2">
                              <span className={`font-label-md text-label-md font-bold ${isItemFlashSale ? 'text-error' : 'text-primary'}`}>
                                Rp {product.price.toLocaleString("id-ID")}
                              </span>
                            </div>
                            {isItemFlashSale && (
                              <span className="text-xs text-on-surface-variant line-through block">
                                Rp {(product.originalPrice || Math.round(product.price * 1.3)).toLocaleString("id-ID")}
                              </span>
                            )}
                          </div>

                          <div className="flex flex-row gap-2 mt-auto">
                            <button
                              onClick={async (e) => {
                                e.stopPropagation();
                                onCheckout([product]);
                              }}
                              className="w-full py-2 bg-surface border border-primary text-primary font-label-sm text-label-sm rounded-md hover:bg-primary-container/10 transition-colors"
                            >
                              Checkout
                            </button>

                            <button
                              onClick={async (e) => {
                                e.stopPropagation();
                                await onAddToCart(product.id, 1);
                                onCartClick();
                              }}
                              className={`w-full py-2 border font-label-sm text-label-sm rounded-md transition-colors ${
                                isItemFlashSale 
                                  ? 'bg-error border-error text-on-error hover:bg-error/90' 
                                  : 'bg-primary border-primary text-white'
                              }`}
                            >
                              Add to Cart
                            </button>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>

                {/* Navigasi Pagination UI */}
                <div className="flex flex-col items-center gap-4 mt-8">
                  {totalPages > 1 && (
                    <div className="flex justify-center items-center gap-2 flex-wrap">
                      <button
                        onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 0))}
                        disabled={currentPage === 0}
                        className="px-4 py-2 rounded-lg border border-outline-variant bg-surface text-on-surface font-label-md disabled:opacity-40 disabled:cursor-not-allowed hover:bg-surface-container transition-colors flex items-center gap-1"
                      >
                        <span className="material-symbols-outlined text-[18px]">chevron_left</span>
                        Previous
                      </button>

                      <div className="flex items-center gap-1 overflow-x-auto px-2">
                        {getVisiblePageNumbers().map((pageIndex) => (
                          <button
                            key={pageIndex}
                            onClick={() => setCurrentPage(pageIndex)}
                            className={`w-10 h-10 rounded-lg font-label-md transition-colors flex items-center justify-center ${
                              currentPage === pageIndex
                                ? 'bg-primary text-on-primary font-bold shadow-sm'
                                : 'bg-surface border border-outline-variant text-on-surface hover:bg-surface-container'
                            }`}
                          >
                            {pageIndex + 1}
                          </button>
                        ))}
                      </div>

                      <button
                        onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages - 1))}
                        disabled={currentPage >= totalPages - 1}
                        className="px-4 py-2 rounded-lg border border-outline-variant bg-surface text-on-surface font-label-md disabled:opacity-40 disabled:cursor-not-allowed hover:bg-surface-container transition-colors flex items-center gap-1"
                      >
                        Next
                        <span className="material-symbols-outlined text-[18px]">chevron_right</span>
                      </button>
                    </div>
                  )}
                  <span className="text-body-sm text-on-surface-variant text-center">
                    Show Page {currentPage + 1} From {totalPages || 1} ({totalElements} Product)
                  </span>
                </div>
              </>
            )}
          </section>
        </div>
      </main>
    </>
  );
};

export default CatalogPage;