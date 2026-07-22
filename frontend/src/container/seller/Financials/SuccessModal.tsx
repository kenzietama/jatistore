interface SuccessModalProps {
    gatewayRef: string;
    onClose: () => void;
}

export default function SuccessModal({ gatewayRef, onClose }: SuccessModalProps) {
    return (
        <div className="fixed inset-0 bg-on-background/30 backdrop-blur-sm z-[999] flex items-center justify-center p-4">
            <div className="bg-surface-container-lowest border border-outline-variant rounded-lg p-6 max-w-sm w-full shadow-lg text-center flex flex-col items-center">
                <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center mb-4">
                    <span className="material-symbols-outlined text-[28px] text-primary">check_circle</span>
                </div>
                
                <h3 className="text-headline-md font-headline-md text-on-surface mb-2">Withdrawal Initiated</h3>
                
                <p className="text-body-md font-body-md text-on-surface-variant mb-6">
                    Your withdrawal request has been processed successfully.
                </p>

                <div className="bg-surface-container-low border border-outline-variant rounded-lg p-3 w-full mb-6 text-left">
                    <span className="text-label-sm font-label-sm text-outline uppercase tracking-wider block mb-1">Transaction ID</span>
                    <span className="font-mono-data text-body-md text-on-surface">{gatewayRef}</span>
                </div>
                
                <div className="w-full">
                    <button 
                        onClick={onClose}
                        className="w-full px-4 py-2 rounded bg-primary text-on-primary hover:bg-surface-tint transition-colors font-label-md text-label-md"
                    >
                        Done
                    </button>
                </div>
            </div>
        </div>
    );
}
