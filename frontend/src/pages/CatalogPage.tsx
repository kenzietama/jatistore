import React from "react";
import { MOCK_PRODUCTS } from "../data/productsMock";

interface CatalogPageProps {
  onProductClick: (id: string) => void;
  onCartClick: () => void;
  onAddToCart: (id: string) => void; 

  isLoggedIn: boolean;
  onLoginClick: () => void;
  onLogoutClick: () => void;
}

const CatalogPage: React.FC<CatalogPageProps> = ({onProductClick,onCartClick,onAddToCart,isLoggedIn,onLoginClick,onLogoutClick}) => {
//   const handleAddToCart = (productId: string) => {
//     alert(`Produk dengan ID ${productId} berhasil ditambah ke keranjang!`);
//   };

  return (
    <>
      {/* TopNavBar */}
      <header className="bg-surface dark:bg-inverse-surface border-b border-outline-variant dark:border-outline shadow-sm docked full-width top-0 sticky z-50">
        <div className="flex justify-between items-center w-full px-margin-mobile md:px-margin-desktop max-w-container-max mx-auto h-16">
          {/* Brand */}
          <div className="flex items-center gap-4">
            <a className="text-headline-md font-headline-lg font-bold text-primary dark:text-inverse-primary tracking-tight" href="#">JatiStore</a>
          </div>
          {/* Search Bar (Center/Left) */}
          <div className="hidden md:flex flex-1 max-w-2xl mx-gutter">
            <div className="relative w-full">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant">search</span>
              <input className="w-full pl-10 pr-4 py-2 bg-surface-container rounded-full border border-outline-variant focus:border-primary focus:ring-2 focus:ring-primary-fixed focus:outline-none transition-all font-body-sm text-body-sm text-on-surface placeholder:text-on-surface-variant" placeholder="Cari barang di JatiStore..." type="text" />
            </div>
          </div>
          {/* Trailing Actions */}
          <div className="flex items-center gap-stack-md">
            <button className="md:hidden text-on-surface-variant hover:text-primary transition-colors">
              <span className="material-symbols-outlined">search</span>
            </button>
            <div className="relative group">
              <button className="text-on-surface-variant hover:text-primary transition-colors relative flex items-center justify-center w-10 h-10 rounded-full hover:bg-surface-container-high">
                <span className="material-symbols-outlined" data-icon="shopping_cart">shopping_cart</span>
                <span className="absolute top-1 right-1 bg-error text-on-error font-label-sm text-label-sm flex items-center justify-center min-w-[18px] h-[18px] rounded-full px-1">2</span>
              </button>
            </div>
            {/* <div className="hidden sm:block h-6 w-px bg-outline-variant mx-2"></div>
            <button className="hidden sm:flex items-center gap-2 text-on-surface-variant hover:text-primary transition-colors font-label-md text-label-md px-4 py-2 border border-outline-variant rounded-full hover:border-primary">
              Masuk
            </button>
            <button className="hidden sm:flex items-center gap-2 bg-primary text-on-primary font-label-md text-label-md px-4 py-2 rounded-full hover:bg-primary-container transition-colors shadow-sm">
              Daftar
            </button>
            <button className="sm:hidden text-on-surface-variant hover:text-primary transition-colors">
              <span className="material-symbols-outlined" data-icon="account_circle">account_circle</span>
            </button> */}
            <div className="hidden sm:block h-6 w-px bg-outline-variant mx-2"></div>
            
            {/*KONDISI STATUS LOGIN USER */}
            {!isLoggedIn ? (
              <>
                {/* JIKA USER BELUM LOGIN: Tampilkan tombol Masuk & Daftar */}
                <button 
                  onClick={onLoginClick}
                  className="hidden sm:flex items-center gap-2 text-on-surface-variant hover:text-primary transition-colors font-label-md text-label-md px-4 py-2 border border-outline-variant rounded-full hover:border-primary"
                >
                  Masuk
                </button>
                <button 
                  onClick={onLoginClick} // Arahkan pendaftaran demo langsung ke halaman login yang sama
                  className="hidden sm:flex items-center gap-2 bg-primary text-on-primary font-label-md text-label-md px-4 py-2 rounded-full hover:bg-primary-container transition-colors shadow-sm"
                >
                  Daftar
                </button>
                <button 
                  onClick={onLoginClick}
                  className="sm:hidden text-on-surface-variant hover:text-primary transition-colors"
                >
                  <span className="material-symbols-outlined" data-icon="account_circle">account_circle</span>
                </button>
              </>
            ) : (
              <>
                {/* JIKA USER SUDAH LOGIN: Ganti dengan tombol Keluar */}
                <button 
                  onClick={onLogoutClick}
                  className="hidden sm:flex items-center gap-2 border border-error text-error font-label-md text-label-md px-4 py-2 rounded-full hover:bg-error/10 transition-colors shadow-sm"
                >
                  Keluar
                </button>
                <button 
                  onClick={onLogoutClick}
                  className="sm:hidden text-error hover:text-error/80 transition-colors"
                >
                  <span className="material-symbols-outlined">logout</span>
                </button>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="w-full max-w-container-max mx-auto px-margin-mobile md:px-margin-desktop py-stack-lg flex flex-col gap-stack-lg">
        {/* Hero: Promo Banner */}
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
          <div className="flex overflow-x-auto hide-scrollbar gap-stack-sm pb-4 -mx-margin-mobile px-margin-mobile md:mx-0 md:px-0">
            {[
              { icon: "devices", label: "Elektronik" },
              { icon: "checkroom", label: "Fashion" },
              { icon: "kitchen", label: "Rumah" },
              { icon: "face_retouching_natural", label: "Kecantikan" },
              { icon: "sports_esports", label: "Hobi" },
              { icon: "local_mall", label: "Kebutuhan" }
            ].map((cat, idx) => (
              <button key={idx} className="flex flex-col items-center gap-2 min-w-[80px] p-4 rounded-xl bg-surface-container hover:bg-surface-container-high transition-colors border border-outline-variant hover:border-primary group">
                <div className="w-12 h-12 rounded-full bg-primary-container/10 flex items-center justify-center text-primary group-hover:scale-110 transition-transform">
                  <span className="material-symbols-outlined fill">{cat.icon}</span>
                </div>
                <span className="font-label-sm text-label-sm text-on-surface">{cat.label}</span>
              </button>
            ))}
          </div>
        </section>

        {/* Flash Sale Section */}
        <section className="bg-surface-container-low rounded-xl p-stack-md md:p-gutter border border-error-container/50">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-stack-md gap-4">
            <div className="flex items-center gap-4">
              <h3 className="font-headline-md text-headline-md text-on-surface flex items-center gap-2">
                <span className="text-error">⚡</span> Flash Sale
              </h3>
              <div className="bg-error text-on-error font-mono-data text-mono-data px-3 py-1 rounded-md flex items-center gap-1 shadow-sm">
                <span className="material-symbols-outlined text-[16px]">timer</span>
                04:45:19
              </div>
            </div>
            <a className="text-primary font-label-md text-label-md hover:underline flex items-center" href="#">Lihat Semua <span className="material-symbols-outlined text-[18px]">chevron_right</span></a>
          </div>
          
          <div className="flex overflow-x-auto hide-scrollbar gap-stack-md pb-2 -mx-margin-mobile px-margin-mobile md:mx-0 md:px-0">
            {/* Flash Sale Product 1 */}
            <div className="min-w-[200px] w-[200px] bg-surface rounded-lg border border-outline-variant overflow-hidden shadow-sm hover:shadow-md transition-shadow relative">
              <div className="absolute top-2 left-2 bg-error text-on-error font-label-sm text-label-sm px-2 py-0.5 rounded-full z-10">-45%</div>
              <div className="h-40 w-full bg-surface-container relative">
                <img className="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCjLncrBHPvFgio37dwosX2XyRBKc0a5k1cpHm5J4II4NLsERoSZTVySiCqLBkRmJR5Sqs4LFib3amFNIYE5PfESb0X5kURYt5594FK0ofWO-SKExbmTr-bWAFUz2oMGdAgOD6n2GZaA_RU7oJzS_quIwmv7ptAYdaTcHYdjbjfpwWCoGKAMojacfGf1ywCpnG7FAoixGPgafG0mnRLyGbIyFMTLaiY2RhM82VOXxJfPBE9KUdMyjU-QwWpyjOWF2xGWtEisbU38IY3" alt="Smartwatch" />
              </div>
              <div className="p-3 flex flex-col gap-2">
                <span className="text-label-sm font-label-sm text-on-surface-variant mb-1 block">Elektronik Store</span>
                <h4 className="font-body-sm text-body-sm text-on-surface line-clamp-2 h-[50px]">Smartwatch Series 8 Active</h4>
                <div>
                  <span className="font-label-md text-label-md text-error block">Rp 1.450.000</span>
                  <span className="font-label-sm text-label-sm text-on-surface-variant line-through">Rp 2.636.000</span>
                </div>
                <div className="mt-2">
                  <div className="w-full bg-surface-variant rounded-full h-1.5 mb-1 overflow-hidden">
                    <div className="bg-error h-1.5 rounded-full w-[80%]"></div>
                  </div>
                  <span className="font-label-sm text-label-sm text-error block text-xs">Stok Terbatas (Sisa 12)</span>
                </div>
              </div>
            </div>

            {/* Flash Sale Product 2 */}
            <div className="min-w-[200px] w-[200px] bg-surface rounded-lg border border-outline-variant overflow-hidden shadow-sm hover:shadow-md transition-shadow relative">
              <div className="absolute top-2 left-2 bg-error text-on-error font-label-sm text-label-sm px-2 py-0.5 rounded-full z-10">-50%</div>
              <div className="h-40 w-full bg-surface-container relative">
                <img className="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAqpgAA8e4bfcjxXzTltJfvnFh_u43kXYmc0cBTbHTXYQUPU0QJYTdyyve_ueJQZNvzSxTnRivzM5PETzqASdPsyX0NohcumYzx6lKmoFLMvwIa8jpvTR7SCAMToiT6BT6Tlr9MhUdycT5mC4_b2x1jrDh_nMYAwjdgg2Jcf0AVKPJzGbzFoaczW3S4eb1b0vFUHswhEt3grGctCqtIoFhSSzH0As3CdtTuGjWIWWutxXrzSljIpvMwSd1MDsMzxlaNef7SCTrMo0XC" alt="Headphones" />
              </div>
              <div className="p-3 flex flex-col gap-2">
                <span className="text-label-sm font-label-sm text-on-surface-variant mb-1 block">Gadget Corner</span>
                <h4 className="font-body-sm text-body-sm text-on-surface line-clamp-2 h-[50px]">Noise Cancelling Headphones Pro</h4>
                <div>
                  <span className="font-label-md text-label-md text-error block">Rp 899.000</span>
                  <span className="font-label-sm text-label-sm text-on-surface-variant line-through">Rp 1.798.000</span>
                </div>
                <div className="mt-2">
                  <div className="w-full bg-surface-variant rounded-full h-1.5 mb-1 overflow-hidden">
                    <div className="bg-error h-1.5 rounded-full w-[95%]"></div>
                  </div>
                  <span className="font-label-sm text-label-sm text-error block text-xs">Stok Terbatas (Sisa 3)</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* General Products Section (Dinamis menggunakan MOCK_PRODUCTS) */}
        <section>
          <div className="flex justify-between items-center mb-stack-md">
            <h3 className="font-headline-md text-headline-md text-on-surface">Produk Terlaris</h3>
            <a className="text-primary font-label-md text-label-md hover:underline" href="#">Muat Lebih Banyak</a>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-stack-sm md:gap-stack-md">
            {MOCK_PRODUCTS.map((product) => (
                <div 
                key={product.id} 
                onClick={() => onProductClick(product.id)} 
                className="bg-surface rounded-lg border border-outline-variant overflow-hidden shadow-sm hover:shadow-md transition-shadow group flex flex-col h-full cursor-pointer"
                >
                <div className="h-48 w-full bg-surface-container relative overflow-hidden">
                    <img className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" src={product.image} alt={product.name} />
                </div>
                <div className="p-3 flex flex-col flex-1">
                    <span className="text-label-sm font-label-sm text-on-surface-variant mb-1 block">JatiStore Official</span>
                    <h4 className="font-body-sm text-body-sm text-on-surface line-clamp-2 mb-2 flex-1">{product.name}</h4>
                    <span className="font-label-md text-label-md text-primary mb-3 block">Rp {product.price.toLocaleString("id-ID")}</span>
                    
                    <button onClick={(e) => { e.stopPropagation(); onAddToCart(product.id); }}
                    className="w-full py-2 bg-surface border border-primary text-primary font-label-sm text-label-sm rounded-md hover:bg-primary-container/10 transition-colors mt-auto"
                    >
                    Tambah Keranjang
                    </button>
                </div>
                </div>
            ))}
            </div>
        </section>
      </main>
    </>
  );
};

export default CatalogPage;