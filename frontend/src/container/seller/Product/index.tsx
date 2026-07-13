import { useState } from 'react';
import { SellerLayout } from '../../../components/layout/seller/SellerLayout';
import { ProductTable } from '../../../components/seller/product/ProductTable';
import { AddProductModal } from '../../../components/seller/product/AddProductModal';

export default function ProductManagement() {
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

  return (
    <SellerLayout>
      <div className="w-full h-full flex flex-col gap-stack-lg min-h-[calc(100vh-100px)]">
        {/* Header Section */}
        <header className="flex justify-between items-center w-full mt-stack-md">
          <div>
            <h2 className="font-headline-lg text-headline-lg text-on-surface">Product Management</h2>
            <p className="font-body-sm text-body-sm text-on-surface-variant mt-unit">Manage your inventory, pricing, and product visibility.</p>
          </div>
          <button 
            onClick={() => setIsAddModalOpen(true)}
            className="bg-primary text-on-primary font-label-md text-label-md px-stack-md py-stack-sm rounded-lg flex items-center gap-unit hover:bg-surface-tint transition-colors shadow-sm"
          >
            <span className="material-symbols-outlined text-[18px]">add</span>
            Add Product
          </button>
        </header>

        {/* Main Content */}
        <ProductTable />

        {/* Modal */}
        <AddProductModal 
          isOpen={isAddModalOpen} 
          onClose={() => setIsAddModalOpen(false)} 
        />
      </div>
    </SellerLayout>
  );
}
