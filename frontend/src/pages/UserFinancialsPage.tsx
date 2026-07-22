import { useEffect, useState } from "react";
import api from "../lib/api";

export default function UserFinancialsPage() {
  const [financials, setFinancials] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchFinancials();
  }, []);

  const fetchFinancials = async () => {
    setIsLoading(true);
    try {
      const response = await api.get("/api/v1/user/balance");
      if (response.data && (response.data.restApiResponseHttpCode === 200 || response.data.code === 200)) {
        const data = response.data.restApiResponseData || response.data.data;
        
        // MOCK: Siapkan data kartu dummy untuk keperluan tampilan sementara
        if (!data.cards || data.cards.length === 0) {
          data.cards = [
            { last4: "4242", expiryDate: "12/28" },
            { last4: "8899", expiryDate: "05/27" }
          ];
        }

        setFinancials(data);
      }
    } catch (error) {
      console.error("Failed to fetch user financials", error);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return <div className="p-4 text-on-surface-variant">Loading financials...</div>;
  }

  if (!financials) {
    return <div className="p-4 text-error">Failed to load financial data.</div>;
  }

  return (
    <div className="space-y-gutter">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-gutter">
        {/* Wallet Balance Card */}
        <section className="bg-primary text-on-primary rounded-xl p-stack-lg shadow-md relative overflow-hidden">
          <div className="absolute -right-8 -top-8 w-32 h-32 bg-white/10 rounded-full blur-2xl"></div>
          <div className="absolute -left-8 -bottom-8 w-32 h-32 bg-black/10 rounded-full blur-2xl"></div>
          
          <div className="relative z-10">
            <div className="flex items-center gap-2 mb-2 opacity-90">
              <span className="material-symbols-outlined">account_balance_wallet</span>
              <h3 className="font-label-md uppercase tracking-wider">JatiPay Balance</h3>
            </div>
            
            <p className="font-display-lg mt-4 mb-1">
              Rp {financials.balance?.toLocaleString("id-ID") || 0}
            </p>
            <p className="font-label-md opacity-80 mb-6">Currency: {financials.currency || "IDR"}</p>
          </div>
        </section>

        {/* Saved Cards Section */}
        <section className="bg-surface-container-lowest border border-outline-variant/30 rounded-xl p-stack-lg shadow-sm">
          <div className="flex justify-between items-center mb-6">
            <h3 className="font-headline-md text-on-surface">Saved Cards</h3>
          </div>

          <div className="space-y-4">
            {financials.cards && financials.cards.length > 0 ? (
              financials.cards.map((card: any, index: number) => (
                <div key={index} className="flex items-center gap-4 p-4 border border-outline-variant/50 rounded-xl hover:border-primary/50 transition-colors group">
                  <div className="w-12 h-8 bg-surface-container rounded flex items-center justify-center">
                    <span className="material-symbols-outlined text-primary">credit_card</span>
                  </div>
                  <div className="flex-grow">
                    <p className="font-label-md text-on-surface">•••• •••• •••• {card.last4}</p>
                    <p className="text-body-sm text-on-surface-variant">Expires {card.expiryDate}</p>
                  </div>
                  <button className="opacity-0 group-hover:opacity-100 p-2 text-error hover:bg-error-container/20 rounded-full transition-all">
                    <span className="material-symbols-outlined text-[18px]">delete</span>
                  </button>
                </div>
              ))
            ) : (
              <div className="text-center py-6 text-on-surface-variant bg-surface-container-low rounded-xl border border-dashed border-outline-variant">
                <span className="material-symbols-outlined mb-2 text-outline">credit_card_off</span>
                <p className="font-body-sm">No saved cards found.</p>
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
