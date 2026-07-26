import React, { useState } from 'react';
import { financialsService } from '../../../service/seller/financials.service';

interface WithdrawalModalProps {
    availableBalance: number;
    onClose: () => void;
    onSuccess: (gatewayRef: string) => void;
}

export default function WithdrawalModal({ availableBalance, onClose, onSuccess }: WithdrawalModalProps) {
    const [amountStr, setAmountStr] = useState<string>('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        const withdrawAmount = parseCurrency(amountStr);
        
        if (isNaN(withdrawAmount) || withdrawAmount <= 0) {
            setError("Amount must be greater than zero.");
            return;
        }
        
        if (withdrawAmount > availableBalance) {
            setError("Amount cannot exceed available balance.");
            return;
        }
        
        if (withdrawAmount > 1000000000) {
            setError("Withdrawal amount exceeds maximum limit of 1,000,000,000 IDR.");
            return;
        }

        setIsSubmitting(true);
        try {
            const response = await financialsService.simulateWithdrawal({ amount: withdrawAmount });
            onSuccess(response.mockGatewayRef);
        } catch (err: any) {
            setError(err.message || err.response?.data?.message || err.response?.data?.restApiResponseMessage || "Failed to process withdrawal.");
            setIsSubmitting(false);
        }
    };

    const formatCurrency = (val: number) => {
        return new Intl.NumberFormat('id-ID', {
            style: 'currency',
            currency: 'IDR',
            minimumFractionDigits: 0
        }).format(val);
    };

    const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        // Remove non-digit characters
        const rawValue = e.target.value.replace(/\D/g, '');
        if (!rawValue) {
            setAmountStr('');
            return;
        }
        
        if (rawValue.length > 15) return; // max 15 digits
        
        // Format with thousand separators and Rp prefix
        const numericValue = parseInt(rawValue, 10);
        const formatted = new Intl.NumberFormat('id-ID', {
            style: 'currency',
            currency: 'IDR',
            minimumFractionDigits: 0
        }).format(numericValue);
        
        setAmountStr(formatted);
    };

    const parseCurrency = (str: string): number => {
        const raw = str.replace(/\D/g, '');
        return raw ? parseInt(raw, 10) : 0;
    };

    return (
        <div className="fixed inset-0 bg-on-background/30 backdrop-blur-sm z-[999] flex items-center justify-center p-4">
            <div className="bg-surface-container-lowest border border-outline-variant rounded-lg p-6 max-w-sm w-full shadow-lg">
                <h3 className="text-headline-md font-headline-md text-on-surface mb-2">Withdraw Funds</h3>
                <p className="text-body-md font-body-md text-on-surface-variant mb-6">
                    Available Balance: <strong className="text-primary">{formatCurrency(availableBalance)}</strong>
                </p>
                
                {error && (
                    <div className="bg-error-container text-on-error-container p-3 rounded mb-4 text-sm">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="mb-6">
                        <label className="block text-label-md font-label-md text-on-surface-variant mb-2">
                            Withdrawal Amount (IDR)
                        </label>
                        <input 
                            type="text"
                            value={amountStr}
                            onChange={handleAmountChange}
                            className="w-full px-3 py-2 border border-outline-variant rounded focus:border-primary focus:ring-2 focus:ring-primary-fixed focus:outline-none transition-all"
                            placeholder="Rp 0"
                            required
                            disabled={isSubmitting}
                        />
                    </div>
                    
                    <div className="flex justify-end gap-3">
                        <button 
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 rounded text-on-surface-variant hover:bg-surface-container-high transition-colors font-label-md text-label-md"
                            disabled={isSubmitting}
                        >
                            Cancel
                        </button>
                        <button 
                            type="submit"
                            className="px-4 py-2 rounded bg-primary text-on-primary hover:bg-surface-tint transition-colors font-label-md text-label-md flex items-center justify-center min-w-[100px]"
                            disabled={isSubmitting || !amountStr || parseCurrency(amountStr) <= 0}
                        >
                            {isSubmitting ? (
                                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                            ) : (
                                "Withdraw"
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
