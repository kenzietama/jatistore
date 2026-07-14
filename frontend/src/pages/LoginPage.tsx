import React from "react";

interface LoginPageProps {
  onLoginSuccess: () => void;
  onBackToCatalog: () => void;
}


const LoginPage: React.FC<LoginPageProps> = ({ onLoginSuccess, onBackToCatalog }) => {
  return (
    <div className="flex-grow flex items-center justify-center px-margin-mobile py-stack-lg bg-surface-container-low min-h-[calc(100vh-4rem)]">
      <div className="w-full max-w-md bg-surface-container-lowest border border-outline-variant rounded-xl p-gutter shadow-sm">
        <div className="text-center mb-6">
          <h2 className="text-headline-lg font-headline-lg text-on-background">Selamat Datang</h2>
          <p className="text-body-sm font-body-sm text-on-surface-variant mt-1">Silakan login untuk mengakses keranjang belanja Anda.</p>
        </div>
        
        <div className="flex flex-col gap-stack-md">
          <button 
            onClick={onLoginSuccess}
            className="w-full bg-primary hover:bg-primary/90 text-on-primary font-label-md text-label-md py-3 rounded-full transition-colors flex items-center justify-center gap-2 shadow-sm"
          >
            <span className="material-symbols-outlined">login</span>
            Masuk dengan Akun Demo
          </button>
          
          <button 
            onClick={onBackToCatalog}
            className="w-full bg-transparent border border-outline text-on-surface hover:bg-surface-container font-label-md text-label-md py-3 rounded-full transition-colors"
          >
            Kembali ke Katalog
          </button>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;