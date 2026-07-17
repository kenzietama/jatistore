import React, { useState, useEffect } from "react";
import api from "../lib/api";

interface Product {
  id: string;
  name: string;
  description: string;
  image: string;
  price: number;
  stock: number;
  store?: {
    id: string;
    storeName: string; 
  };
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
      console.error("Gagal memuat jumlah keranjang:", error);
    }
  };

  useEffect(() => {
      api.get("/api/v1/products/list")
      .then((response) => {
        if (response.data && response.data.code === 200) {
          setProducts(response.data.data);
        }
        setIsLoading(false);
      })
      .catch((error) => {
        console.error("Gagal memuat data produk dari Postgres:", error);
        setIsLoading(false);
      });
      api.get("/api/v1/categories")
      .then((res) => {
        if (res.data?.code === 200) setCategories(res.data.data);
      })
      .catch((err) => console.error("Gagal ambil kategori:", err));

    fetchCartCount();
  }, []);

//   const filteredProducts = products.filter((product) => {
//   const matchesSearch = product.name?.toLowerCase().includes(searchQuery.toLowerCase());
  
//   // Gunakan ID untuk perbandingan agar lebih akurat
//   const matchesCategory = selectedCategoryId 
//     ? (product.category?.id === selectedCategoryId || product.categoryId === selectedCategoryId)
//     : true;
    
//   return matchesSearch && matchesCategory;
// });

const filteredProducts = products.filter((product) => {
  const matchesSearch = product.name?.toLowerCase().includes(searchQuery.toLowerCase());
  
  // LOG INI SANGAT PENTING
  console.log("Produk:", product.name, "| CategoryID di Produk:", product.category?.id || product.categoryId, "| SelectedID:", selectedCategoryId);

  const matchesCategory = selectedCategoryId 
    ? (String(product.category?.id) === String(selectedCategoryId) || String(product.categoryId) === String(selectedCategoryId))
    : true;
    
  return matchesSearch && matchesCategory;
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
              <h2 className="font-display-lg text-display-lg text-on-primary mb-2 leading-tight">Gelar Diskon Tengah Tahun</h2>
              <h3 className="font-headline-lg text-headline-lg text-on-primary/90 mb-4">TECH & STYLE FESTIVAL</h3>
              <p className="font-body-lg text-body-lg text-on-primary/90 mb-6">Diskon hingga 70% untuk semua kategori elektronik dan fashion.</p>
              <button className="bg-surface text-primary font-label-md text-label-md px-6 py-3 rounded-full hover:bg-primary-fixed transition-colors shadow-sm">Belanja Sekarang</button>
            </div>
          </div>
        </section>

        {/* Categories */}
        <section>
          <div className="flex overflow-x-auto hide-scrollbar gap-stack-sm pb-4">
            <button 
              onClick={() => setSelectedCategoryId(null)}
              className={`p-4 rounded-xl border ${selectedCategoryId === null ? 'bg-primary text-on-primary' : 'bg-surface-container'}`}
            >
              Semua
            </button>
            
            {categories.map((cat) => (
              <button 
                key={cat.id} 
                onClick={() => setSelectedCategoryId(cat.id)}
                className={`flex flex-col items-center gap-2 p-4 rounded-xl border ${selectedCategoryId === cat.id ? 'border-primary bg-primary-container/20' : 'bg-surface-container'}`}
              >
                <span className="material-symbols-outlined">{cat.icon}</span>
                <span className="font-label-sm">{cat.name}</span>
              </button>
            ))}
          </div> {/* Div penutup untuk flex */}
        </section> {/* Section penutup untuk kategori */}

        {/* General Products Section */}
        <section>
          <div className="flex justify-between items-center mb-stack-md">
            <h3 className="font-headline-md text-headline-md text-on-surface">Produk Terlaris</h3>
            <a className="text-primary font-label-md text-label-md hover:underline" href="#">Muat Lebih Banyak</a>
          </div>

          {isLoading ? (
            <div className="text-center py-10 font-label-md text-on-surface-variant">Memuat produk dari database...</div>
          ) : filteredProducts.length === 0 ? (
            // Feedback jika pencarian barang di katalog kosong
            <div className="text-center py-12 bg-surface-container-lowest border border-outline-variant rounded-lg">
              <span className="material-symbols-outlined text-[48px] text-on-surface-variant/40">search_off</span>
              <p className="text-body-lg font-body-lg text-on-surface-variant mt-2">Produk "{searchQuery}" tidak dapat ditemukan</p>
            </div>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-stack-sm md:gap-stack-md">
              {/* Merender dari array filteredProducts yang terhubung dengan search bar global */}
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
                      {product.store?.storeName || "Toko Tidak Dikenal"}
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
                        Tambah Keranjang
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </main>
    </>
  );
};

export default CatalogPage;