import React, { useState, useEffect, useCallback } from 'react';
import { SellerLayout } from '../../components/layout/seller/SellerLayout';
import { sellerFlashSaleService, type AvailableFlashSaleResponse, type SellerFlashSaleItemResponse, type SellerFlashSaleItemsWrapperResponse } from '../../service/seller/flash-sale.service';
import { productService, type Product } from '../../service/seller/product.service';

export default function FlashSaleManager() {
    const [events, setEvents] = useState<AvailableFlashSaleResponse[]>([]);
    const [selectedEventId, setSelectedEventId] = useState<string>('');
    const [products, setProducts] = useState<Product[]>([]);
    
    const [configuredItems, setConfiguredItems] = useState<SellerFlashSaleItemResponse[]>([]);
    const [loadingItems, setLoadingItems] = useState(false);

    // Form states
    const [selectedProductId, setSelectedProductId] = useState<string>('');
    const [flashPrice, setFlashPrice] = useState<string>('');
    const [promoStock, setPromoStock] = useState<string>('');
    const [isSaving, setIsSaving] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [itemToDelete, setItemToDelete] = useState<SellerFlashSaleItemResponse | null>(null);

    const [itemToEdit, setItemToEdit] = useState<SellerFlashSaleItemResponse | null>(null);

    useEffect(() => {
        fetchInitialData();
    }, []);

    const fetchInitialData = async () => {
        try {
            const fetchedEvents = await sellerFlashSaleService.getAvailableFlashSales();
            setEvents(fetchedEvents);
            const fetchedProducts = await productService.getProducts(undefined, undefined, undefined, undefined, undefined, 0, 100);
            setProducts(fetchedProducts.content);
        } catch (error: any) {
            console.error('Failed to fetch initial data', error);
        }
    };

    const fetchConfiguredItems = useCallback(async (eventId: string) => {
        if (!eventId) {
            setConfiguredItems([]);
            return;
        }
        try {
            setLoadingItems(true);
            const data = await sellerFlashSaleService.getFlashSaleItems(eventId, 0, 100);
            setConfiguredItems(data.items || []);
        } catch (error: any) {
            console.error('Failed to fetch configured items', error);
        } finally {
            setLoadingItems(false);
        }
    }, []);

    useEffect(() => {
        fetchConfiguredItems(selectedEventId);
    }, [selectedEventId, fetchConfiguredItems]);

    const handleEventChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setSelectedEventId(e.target.value);
        setErrorMsg('');
    };

    const handleProductChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const pId = e.target.value;
        setSelectedProductId(pId);
        
        const prod = products.find(p => p.id === pId);
        if (prod && !itemToEdit) {
            // reset form defaults when product changes
            setFlashPrice('');
            setPromoStock('');
        }
    };

    const cancelEdit = () => {
        setSelectedProductId('');
        setFlashPrice('');
        setPromoStock('');
        setItemToEdit(null);
        setErrorMsg('');
    };

    const handleSaveItem = async () => {
        if (!selectedEventId) {
            setErrorMsg('Please select a flash sale event first.');
            return;
        }
        if (!selectedProductId || !flashPrice || !promoStock) {
            setErrorMsg('Please fill out all product details.');
            return;
        }

        try {
            setIsSaving(true);
            setErrorMsg('');
            
            if (itemToEdit) {
                await sellerFlashSaleService.updateFlashSaleItem(selectedEventId, itemToEdit.productId, {
                    productId: itemToEdit.productId,
                    flashPrice: parseFloat(flashPrice),
                    remainingQuota: parseInt(promoStock)
                });
            } else {
                await sellerFlashSaleService.addFlashSaleItem(selectedEventId, {
                    productId: selectedProductId,
                    flashPrice: parseFloat(flashPrice),
                    remainingQuota: parseInt(promoStock)
                });
            }
            
            // Refresh table
            await fetchConfiguredItems(selectedEventId);
            
            // Reset form
            cancelEdit();
        } catch (error: any) {
            setErrorMsg(error.response?.data?.message || (itemToEdit ? 'Failed to update item.' : 'Failed to add item to flash sale.'));
        } finally {
            setIsSaving(false);
        }
    };

    const openDeleteModal = (item: SellerFlashSaleItemResponse) => {
        setItemToDelete(item);
        setIsDeleteModalOpen(true);
    };

    const confirmDelete = async () => {
        if (!selectedEventId || !itemToDelete) return;
        
        try {
            await sellerFlashSaleService.removeFlashSaleItem(selectedEventId, itemToDelete.productId);
            await fetchConfiguredItems(selectedEventId);
            setIsDeleteModalOpen(false);
            setItemToDelete(null);
        } catch (error: any) {
            alert(error.response?.data?.message || 'Failed to remove item.');
        }
    };

    const handleEditInline = (item: SellerFlashSaleItemResponse) => {
        setItemToEdit(item);
        setSelectedProductId(item.productId);
        setFlashPrice(item.flashPrice.toString());
        setPromoStock(item.remainingQuota.toString());
        setErrorMsg('');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const selectedProductDetail = products.find(p => p.id === selectedProductId);

    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR' }).format(amount);
    };

    return (
        <SellerLayout>
            <div className="w-full h-full flex flex-col gap-stack-lg min-h-[calc(100vh-100px)] pt-stack-md">
                
                {/* Page Header & Banner */}
                <div className="mb-gutter flex flex-col gap-stack-md">
                    <div>
                        <h2 className="font-headline-lg text-[32px] font-bold text-on-surface m-0">Flash Sale Configuration</h2>
                    </div>

                    {/* Active Window Banner */}
                    <div className="bg-primary-container/10 border border-primary/20 rounded-lg p-stack-md flex flex-col gap-stack-sm">
                        <div className="flex items-center gap-2 text-primary">
                            <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>event</span>
                            <h3 className="text-label-md font-bold uppercase tracking-wider">Select Flash Sale Event</h3>
                        </div>
                        <div className="flex flex-col gap-unit">
                            <label className="text-body-sm font-medium text-on-surface-variant" htmlFor="eventSelect">
                                Choose an upcoming flash sale to participate in
                            </label>
                            <div className="relative">
                                <select 
                                    id="eventSelect"
                                    className="w-full h-10 px-3 bg-surface border border-outline-variant rounded-md text-body-md font-body-md focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all appearance-none outline-none"
                                    value={selectedEventId}
                                    onChange={handleEventChange}
                                >
                                    <option value="" disabled>Select an event...</option>
                                    {events.map(event => (
                                        <option key={event.id} value={event.id}>
                                            {event.name} ({new Date(event.startTime).toLocaleDateString()} - {new Date(event.endTime).toLocaleDateString()}) - [{event.status.toUpperCase()}]
                                        </option>
                                    ))}
                                </select>
                                <span className="material-symbols-outlined absolute right-3 top-2 text-on-surface-variant pointer-events-none">expand_more</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
                    {/* Left Column: Configuration Form */}
                    <div className="lg:col-span-4 flex flex-col gap-gutter">
                        <div className="bg-surface-container-lowest border border-outline-variant rounded-xl p-stack-md shadow-sm">
                            <h2 className="text-headline-md font-headline-md font-semibold text-on-surface mb-stack-md border-b border-outline-variant pb-stack-sm">
                                {itemToEdit ? 'Edit Flash Sale Item' : 'Add Item to Flash Sale'}
                            </h2>
                            
                            {errorMsg && (
                                <div className="mb-4 p-3 bg-error-container text-on-error-container rounded-md text-body-sm font-medium">
                                    {errorMsg}
                                </div>
                            )}

                            <form className="flex flex-col gap-stack-sm">
                                {/* Product Select */}
                                <div className="flex flex-col gap-unit">
                                    <label className="text-label-md font-label-md text-on-surface-variant" htmlFor="productSelect">Select Product</label>
                                    <div className="relative">
                                        <select 
                                            id="productSelect"
                                            className="w-full h-10 px-3 bg-surface border border-outline-variant rounded-md text-body-md font-body-md focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all appearance-none outline-none"
                                            value={selectedProductId}
                                            onChange={handleProductChange}
                                            disabled={!selectedEventId || !!itemToEdit}
                                        >
                                            <option value="" disabled>Choose a product from inventory...</option>
                                            {products.map(p => (
                                                <option key={p.id} value={p.id}>{p.name}</option>
                                            ))}
                                        </select>
                                        <span className="material-symbols-outlined absolute right-3 top-2 text-on-surface-variant pointer-events-none">expand_more</span>
                                    </div>
                                </div>

                                {/* Flash Price */}
                                <div className="flex flex-col gap-unit">
                                    <label className="text-label-md font-label-md text-on-surface-variant" htmlFor="flashPrice">Flash Price</label>
                                    <div className="relative">
                                        <span className="material-symbols-outlined absolute left-3 top-2 text-on-surface-variant pointer-events-none text-[20px]">payments</span>
                                        <input 
                                            type="number"
                                            id="flashPrice"
                                            className="w-full h-10 pl-9 pr-3 bg-surface border border-outline-variant rounded-md text-body-md font-body-md focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all outline-none"
                                            placeholder="0"
                                            value={flashPrice}
                                            onChange={e => setFlashPrice(e.target.value)}
                                            disabled={!selectedProductId}
                                        />
                                    </div>
                                    <span className="text-label-sm font-label-sm text-outline">
                                        Current retail: <span className="font-mono-data text-primary font-medium">{selectedProductDetail ? formatCurrency(selectedProductDetail.price) : '--'}</span>
                                    </span>
                                </div>

                                {/* Promo Stock */}
                                <div className="flex flex-col gap-unit mb-stack-sm">
                                    <label className="text-label-md font-label-md text-on-surface-variant" htmlFor="promoStock">Promo Stock Quantity</label>
                                    <input 
                                        type="number"
                                        id="promoStock"
                                        className="w-full h-10 px-3 bg-surface border border-outline-variant rounded-md text-body-md font-body-md focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all outline-none"
                                        placeholder="Enter quantity"
                                        value={promoStock}
                                        onChange={e => setPromoStock(e.target.value)}
                                        disabled={!selectedProductId}
                                    />
                                    <span className="text-label-sm font-label-sm text-outline">
                                        Available in warehouse: <span className="font-mono-data text-primary font-medium">{selectedProductDetail ? selectedProductDetail.stock : '--'}</span>
                                    </span>
                                </div>

                                <div className="flex flex-col gap-2 mt-2">
                                    <button 
                                        type="button"
                                        onClick={handleSaveItem}
                                        disabled={!selectedEventId || !selectedProductId || isSaving}
                                        className="w-full h-10 bg-primary text-on-primary rounded-md font-label-md text-label-md hover:bg-surface-tint transition-colors flex items-center justify-center gap-2 shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        <span className="material-symbols-outlined text-[18px]">save</span>
                                        {isSaving ? 'Saving...' : (itemToEdit ? 'Update Flash Sale Item' : 'Save Flash Sale Item')}
                                    </button>
                                    
                                    {itemToEdit && (
                                        <button 
                                            type="button"
                                            onClick={cancelEdit}
                                            disabled={isSaving}
                                            className="w-full h-10 bg-surface-variant text-on-surface-variant rounded-md font-label-md text-label-md hover:bg-outline-variant/30 transition-colors flex items-center justify-center gap-2 shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
                                        >
                                            <span className="material-symbols-outlined text-[18px]">close</span>
                                            Cancel Edit
                                        </button>
                                    )}
                                </div>
                            </form>
                        </div>

                        {/* Informational Widget */}
                        <div className="bg-surface-container border border-outline-variant rounded-xl p-stack-md flex flex-col gap-stack-sm">
                            <div className="flex items-center gap-2 text-secondary">
                                <span className="material-symbols-outlined">info</span>
                                <h3 className="font-headline-sm text-headline-sm font-semibold">Flash Sale Rules</h3>
                            </div>
                            <ul className="list-disc pl-5 text-body-sm font-body-sm text-on-surface-variant flex flex-col gap-1">
                                <li>Flash price must be less than current retail price.</li>
                                <li>Promo stock cannot exceed available warehouse stock.</li>
                                <li>Products cannot be part of overlapping flash sales.</li>
                            </ul>
                        </div>
                    </div>

                    {/* Right Column: Configured Products Table */}
                    <div className="lg:col-span-8">
                        <div className="bg-surface-container-lowest border border-outline-variant rounded-xl shadow-sm overflow-hidden flex flex-col h-full">
                            <div className="p-stack-md border-b border-outline-variant flex justify-between items-center bg-surface-container-low">
                                <h2 className="text-headline-md font-headline-md text-on-surface font-semibold">Configured Products</h2>
                                {selectedEventId && (
                                    <span className="bg-secondary-container text-on-secondary-container px-2 py-1 rounded text-label-sm font-label-sm font-bold">
                                        {configuredItems.length} Items Added
                                    </span>
                                )}
                            </div>

                            {!selectedEventId ? (
                                <div className="p-stack-lg flex flex-col items-center justify-center text-center my-10">
                                    <span className="material-symbols-outlined text-display-lg text-outline-variant mb-2">event_busy</span>
                                    <p className="text-body-md font-body-md text-on-surface-variant">Please select a flash sale event first.</p>
                                </div>
                            ) : loadingItems ? (
                                <div className="p-stack-lg flex flex-col items-center justify-center text-center my-10">
                                    <span className="material-symbols-outlined text-display-lg text-primary animate-spin mb-2">progress_activity</span>
                                    <p className="text-body-md font-body-md text-on-surface-variant">Loading configured items...</p>
                                </div>
                            ) : configuredItems.length === 0 ? (
                                <div className="p-stack-lg flex flex-col items-center justify-center text-center my-10">
                                    <span className="material-symbols-outlined text-display-lg text-outline-variant mb-2">inventory_2</span>
                                    <p className="text-body-md font-body-md text-on-surface-variant">No items configured yet.</p>
                                </div>
                            ) : (
                                <div className="overflow-x-auto">
                                    <table className="w-full text-left border-collapse">
                                        <thead>
                                            <tr className="bg-surface-variant text-on-surface-variant text-label-sm font-label-sm uppercase tracking-wider">
                                                <th className="p-3 font-medium">Product</th>
                                                <th className="p-3 font-medium text-right">Retail Price</th>
                                                <th className="p-3 font-medium text-right">Flash Price</th>
                                                <th className="p-3 font-medium text-right">Promo Stock</th>
                                                <th className="p-3 font-medium text-center">Status</th>
                                                <th className="p-3 font-medium text-center">Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody className="text-body-sm font-body-sm">
                                            {configuredItems.map((item, index) => (
                                                <tr key={item.itemId} className={`border-b border-outline-variant hover:bg-surface-container-low transition-colors group ${index % 2 === 1 ? 'bg-surface-bright' : ''}`}>
                                                    <td className="p-3 flex items-center gap-3">
                                                        <div className="w-10 h-10 rounded bg-surface-variant overflow-hidden shrink-0 border border-outline-variant/50">
                                                            {item.productImage ? (
                                                                <img src={item.productImage} alt={item.productName} className="w-full h-full object-cover" />
                                                            ) : (
                                                                <span className="material-symbols-outlined w-full h-full flex items-center justify-center text-outline">image</span>
                                                            )}
                                                        </div>
                                                        <span className="font-medium text-on-surface line-clamp-2">{item.productName}</span>
                                                    </td>
                                                    <td className="p-3 text-right text-on-surface-variant line-through font-mono-data">
                                                        {formatCurrency(item.originalPrice)}
                                                    </td>
                                                    <td className="p-3 text-right font-bold text-primary font-mono-data">
                                                        {formatCurrency(item.flashPrice)}
                                                    </td>
                                                    <td className="p-3 text-right font-mono-data">{item.remainingQuota}</td>
                                                    <td className="p-3 text-center">
                                                        <span className="inline-block px-2 py-1 bg-surface-tint/10 border border-surface-tint/30 text-surface-tint rounded text-[10px] font-mono-data uppercase font-bold tracking-wider">
                                                            Ready
                                                        </span>
                                                    </td>
                                                    <td className="p-3 text-center flex items-center justify-center gap-2">
                                                        <button 
                                                            onClick={() => handleEditInline(item)}
                                                            className="text-outline hover:text-primary transition-colors p-1" 
                                                            title="Edit"
                                                        >
                                                            <span className="material-symbols-outlined text-[18px]">edit</span>
                                                        </button>
                                                        <button 
                                                            onClick={() => openDeleteModal(item)}
                                                            className="text-outline hover:text-error transition-colors p-1" 
                                                            title="Remove"
                                                        >
                                                            <span className="material-symbols-outlined text-[18px]">delete</span>
                                                        </button>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Delete Confirmation Modal */}
            {isDeleteModalOpen && (
                <div className="fixed inset-0 bg-on-background/30 backdrop-blur-sm z-[999] flex items-center justify-center p-4">
                    <div className="bg-surface-container-lowest border border-outline-variant rounded-lg p-6 max-w-sm w-full shadow-lg">
                        <h3 className="text-headline-md font-headline-md text-on-surface mb-2">Confirm Delete</h3>
                        <p className="text-body-md font-body-md text-on-surface-variant mb-6">Are you sure you want to remove this product from the flash sale?</p>
                        <div className="flex justify-end gap-3">
                            <button 
                                onClick={() => setIsDeleteModalOpen(false)}
                                className="px-4 py-2 rounded text-on-surface-variant hover:bg-surface-container-high transition-colors font-label-md text-label-md"
                            >
                                Cancel
                            </button>
                            <button 
                                onClick={confirmDelete}
                                className="px-4 py-2 rounded bg-error text-on-error hover:opacity-90 transition-opacity font-label-md text-label-md flex items-center gap-2"
                            >
                                <span className="material-symbols-outlined text-[18px]">delete</span>
                                Remove
                            </button>
                        </div>
                    </div>
                </div>
            )}

        </SellerLayout>
    );
}
