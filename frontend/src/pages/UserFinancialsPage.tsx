import { useEffect, useState } from "react";
import api from "../lib/api";

export default function UserFinancialsPage() {
  const [balance, setBalance] = useState<number>(0);
  const [currency, setCurrency] = useState<string>("IDR");
  const [cards, setCards] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deletingCardId, setDeletingCardId] = useState<string | number | null>(null);
  const [cardToDelete, setCardToDelete] = useState<any | null>(null);

  useEffect(() => {
    fetchFinancials();
  }, []);

  const fetchFinancials = async () => {
    setIsLoading(true);
    setError(null);
    try {
      // Fetch balance
      const balanceRes = await api.get("/api/v1/user/balance");
      if (balanceRes.data && (balanceRes.data.restApiResponseHttpCode === 200 || balanceRes.data.code === 200)) {
        const bData = balanceRes.data.restApiResponseData || balanceRes.data.data || {};
        setBalance(bData.balance ?? 0);
        setCurrency(bData.currency || "IDR");
      }

      // Fetch saved cards
      const cardsRes = await api.get("/api/v1/user/cards");
      if (cardsRes.data) {
        const cardData = cardsRes.data.data || cardsRes.data.restApiResponseData || cardsRes.data;
        if (Array.isArray(cardData)) {
          setCards(cardData);
        } else {
          setCards([]);
        }
      }
    } catch (err) {
      console.error("Failed to fetch user financials", err);
      setError("Failed to load financial data.");
    } finally {
      setIsLoading(false);
    }
  };

  const confirmDeleteCard = async () => {
    if (!cardToDelete) return;
    setDeletingCardId(cardToDelete.id);
    try {
      await api.delete("/api/v1/user/cards/" + cardToDelete.id);
      setCards((prev) => prev.filter((c) => c.id !== cardToDelete.id));
    } catch (err) {
      console.error("Failed to delete card", err);
    } finally {
      setDeletingCardId(null);
      setCardToDelete(null);
    }
  };

  if (isLoading) {
    return <div className="p-4 text-on-surface-variant flex items-center gap-2">
      <span className="material-symbols-outlined animate-spin">progress_activity</span>
      Loading financials...
    </div>;
  }

  if (error && !balance && cards.length === 0) {
    return <div className="p-4 text-error">{error}</div>;
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
              Rp {balance.toLocaleString("id-ID")}
            </p>
            <p className="font-label-md opacity-80 mb-6">Currency: {currency}</p>
          </div>
        </section>

        {/* Saved Cards Section */}
        <section className="bg-surface-container-lowest border border-outline-variant/30 rounded-xl p-stack-lg shadow-sm">
          <div className="flex justify-between items-center mb-6">
            <h3 className="font-headline-md text-on-surface flex items-center gap-2 font-bold">
              <span className="material-symbols-outlined text-primary">credit_card</span>
              Saved Cards
            </h3>
          </div>

          <div className="space-y-4">
            {cards.length > 0 ? (
              cards.map((card: any) => (
                <div key={card.id || card.last4} className="flex items-center gap-4 p-4 border border-outline-variant/50 rounded-xl hover:border-primary/50 transition-colors group">
                  <div className="w-12 h-8 bg-surface-container rounded flex items-center justify-center">
                    <span className="material-symbols-outlined text-primary">credit_card</span>
                  </div>
                  <div className="flex-grow">
                    <p className="font-label-md text-on-surface font-semibold">•••• •••• •••• {card.last4}</p>
                    <p className="text-body-sm text-on-surface-variant">
                      {card.cardHolderName || card.card_holder_name || "Cardholder"} • Expires {card.expiryDate || card.expiry_date}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => setCardToDelete(card)}
                    disabled={deletingCardId === card.id}
                    className="opacity-0 group-hover:opacity-100 p-2 text-error hover:bg-error-container/20 rounded-full transition-all"
                    title="Delete card"
                  >
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

      {/* Delete Confirmation Modal */}
      {cardToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-on-background/40 backdrop-blur-sm p-4">
          <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl p-6 max-w-md w-full shadow-lg">
            <div className="flex items-center gap-3 text-error mb-4">
              <span className="material-symbols-outlined text-[28px]">warning</span>
              <h3 className="font-headline-md text-on-surface font-bold">Delete Saved Card</h3>
            </div>
            <p className="text-body-md text-on-surface-variant mb-6">
              Are you sure you want to remove card ending in <strong className="text-on-surface">•••• {cardToDelete.last4}</strong>? This action cannot be undone.
            </p>
            <div className="flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setCardToDelete(null)}
                className="px-4 py-2 rounded-full border border-outline-variant text-on-surface font-label-md hover:bg-surface-container-low transition-colors"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={confirmDeleteCard}
                disabled={deletingCardId === cardToDelete.id}
                className="px-4 py-2 rounded-full bg-error text-on-error font-label-md hover:opacity-90 transition-opacity flex items-center gap-2"
              >
                {deletingCardId === cardToDelete.id && (
                  <span className="material-symbols-outlined text-[18px] animate-spin">progress_activity</span>
                )}
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

