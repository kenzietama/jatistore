import { useState } from 'react';
import { SellerLayout } from '../../../components/layout/seller/SellerLayout';
import { ProductTable } from '../../../components/seller/product/ProductTable';
import { AddProductModal } from '../../../components/seller/product/AddProductModal';
import type { Product } from '../../../service/seller/product.service';

export default function ProductManagement() {
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleEdit = (product: Product) => {
    setEditingProduct(product);
    setIsAddModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsAddModalOpen(false);
    setTimeout(() => setEditingProduct(null), 300); // clear after animation
  };

  return (
    <SellerLayout>
      <div className="w-full h-full flex flex-col gap-stack-lg min-h-[calc(100vh-100px)]">
        {/* Header Section */}
        <header className="flex justify-between items-center w-full mt-stack-md">
          <h2 className="font-headline-lg text-[32px] font-bold text-on-surface m-0">Product Management</h2>
          <button 
            onClick={() => setIsAddModalOpen(true)}
            className="bg-primary text-on-primary font-label-md text-label-md px-stack-md py-stack-sm rounded-lg flex items-center gap-unit hover:bg-surface-tint transition-colors shadow-sm"
          >
            <span className="material-symbols-outlined text-[18px]">add</span>
            Add Product
          </button>
        </header>

        {/* Main Content */}
        <ProductTable refreshTrigger={refreshTrigger} onEdit={handleEdit} />

        {/* Modal */}
        <AddProductModal 
          isOpen={isAddModalOpen} 
          onClose={handleCloseModal} 
          onSuccess={() => setRefreshTrigger(prev => prev + 1)}
          initialData={editingProduct}
        />
      </div>
    </SellerLayout>
  );
}
