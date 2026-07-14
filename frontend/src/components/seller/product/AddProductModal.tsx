import React, { useState, useEffect } from 'react';
import type { Product } from '../../../service/seller/product.service';
import { productService } from '../../../service/seller/product.service';

interface AddProductModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  initialData?: Product | null;
}

export function AddProductModal({ isOpen, onClose, onSuccess, initialData }: AddProductModalProps) {
  const [isDragging, setIsDragging] = useState(false);
  const [name, setName] = useState('');
  const [price, setPrice] = useState('');
  const [stock, setStock] = useState('');
  const [category, setCategory] = useState('');
  const [description, setDescription] = useState('');
  const [image, setImage] = useState<File | null>(null);
  
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const isEdit = !!initialData;

  useEffect(() => {
    if (isOpen) {
      if (initialData) {
        setName(initialData.name);
        setPrice(initialData.price.toString());
        setStock(initialData.stock.toString());
        setCategory(initialData.categoryId);
        setDescription(initialData.description || '');
        setImage(null);
      } else {
        setName('');
        setPrice('');
        setStock('');
        setCategory('');
        setDescription('');
        setImage(null);
        setError('');
      }
    }
  }, [isOpen, initialData]);

  if (!isOpen) return null;

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      setImage(e.dataTransfer.files[0]);
    }
  };

  const handleSubmit = async () => {
    setError('');
    if (!name || !price || !stock || !category) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      setIsSubmitting(true);
      
      let imageUrl = initialData?.image || '';
      
      if (image) {
        imageUrl = await productService.uploadImage(image);
      }

      const payload = {
        name,
        price: parseFloat(price),
        stock: parseInt(stock, 10),
        productCategoryId: category,
        description,
        image: imageUrl
      };

      if (isEdit && initialData) {
        await productService.updateProduct(initialData.id, payload);
      } else {
        await productService.createProduct(payload);
      }
      
      onSuccess();
      onClose();
    } catch (err) {
      console.error('Error saving product:', err);
      setError('Failed to save product. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-margin-mobile md:p-margin-desktop bg-on-background/30 backdrop-blur-sm overflow-y-auto">
      <div className="bg-surface-container-lowest border border-outline-variant rounded-xl shadow-lg w-full max-w-3xl flex flex-col relative my-auto animate-in fade-in slide-in-from-bottom-4 duration-300">
        
        <div className="flex justify-between items-center p-stack-md border-b border-outline-variant">
          <h3 className="font-title-lg text-title-lg text-on-surface">
            {isEdit ? 'Edit Product' : 'Add New Product'}
          </h3>
          <button 
            onClick={onClose}
            disabled={isSubmitting}
            className="text-on-surface-variant hover:text-on-surface transition-colors p-unit rounded-full hover:bg-surface-container-high disabled:opacity-50"
          >
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        <div className="px-gutter py-stack-lg overflow-y-auto flex-1">
          {error && <div className="mb-4 p-3 bg-error/10 text-error rounded font-body-sm">{error}</div>}
          
          <form className="space-y-stack-md">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-gutter">
              <div className="space-y-stack-md">
                <div className="space-y-unit">
                  <label className="font-label-md text-label-md text-on-surface-variant" htmlFor="product-name">Product Name *</label>
                  <input 
                    className="w-full h-10 px-3 border border-outline-variant rounded-lg bg-surface focus:outline-none focus:border-primary focus:ring-4 focus:ring-primary-container/20 font-body-md text-body-md text-on-surface transition-all" 
                    id="product-name" 
                    placeholder="e.g., Ergonomic Office Chair" 
                    type="text" 
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                  />
                </div>

                <div className="grid grid-cols-2 gap-stack-sm">
                  <div className="space-y-unit">
                    <label className="font-label-md text-label-md text-on-surface-variant" htmlFor="product-price">Price *</label>
                    <div className="relative">
                      <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-on-surface-variant font-label-md">Rp</span>
                      <input 
                        className="w-full h-10 pl-10 pr-3 border border-outline-variant rounded-lg bg-surface focus:outline-none focus:border-primary focus:ring-4 focus:ring-primary-container/20 font-body-md text-body-md text-on-surface transition-all" 
                        id="product-price" 
                        min="0" 
                        placeholder="0" 
                        type="number"
                        value={price}
                        onChange={(e) => setPrice(e.target.value)}
                      />
                    </div>
                  </div>
                  <div className="space-y-unit">
                    <label className="font-label-md text-label-md text-on-surface-variant" htmlFor="product-stock">Initial Stock *</label>
                    <input 
                      className="w-full h-10 px-3 border border-outline-variant rounded-lg bg-surface focus:outline-none focus:border-primary focus:ring-4 focus:ring-primary-container/20 font-body-md text-body-md text-on-surface transition-all" 
                      id="product-stock" 
                      min="0" 
                      placeholder="0" 
                      type="number" 
                      value={stock}
                      onChange={(e) => setStock(e.target.value)}
                    />
                  </div>
                </div>

                <div className="space-y-unit">
                  <label className="font-label-md text-label-md text-on-surface-variant" htmlFor="product-category">Category *</label>
                  <select 
                    className="w-full h-10 px-3 border border-outline-variant rounded-lg bg-surface focus:outline-none focus:border-primary focus:ring-4 focus:ring-primary-container/20 font-body-md text-body-md text-on-surface appearance-none transition-all" 
                    id="product-category" 
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                  >
                    <option disabled value="">Select a category...</option>
                    <option value="dd000000-0000-0000-0000-000000000001">Electronics</option>
                    <option value="dd000000-0000-0000-0000-000000000003">Home &amp; Garden</option>
                    <option value="dd000000-0000-0000-0000-000000000002">Fashion</option>
                    <option value="dd000000-0000-0000-0000-000000000008">Automotive &amp; Tools</option>
                  </select>
                </div>

                <div className="space-y-unit">
                  <label className="font-label-md text-label-md text-on-surface-variant" htmlFor="product-desc">Product Description</label>
                  <textarea 
                    className="w-full p-3 border border-outline-variant rounded-lg bg-surface focus:outline-none focus:border-primary focus:ring-4 focus:ring-primary-container/20 font-body-md text-body-md text-on-surface transition-all resize-none" 
                    id="product-desc" 
                    rows={4}
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                  ></textarea>
                </div>
              </div>

              <div className="space-y-stack-md flex flex-col relative">
                <label className="block font-label-md text-label-md text-on-surface mb-unit">Product Image</label>
                <div 
                  className={`border-2 border-dashed rounded-xl p-stack-md text-center transition-colors cursor-pointer relative ${isDragging ? 'border-primary bg-primary-container/10' : 'border-outline-variant hover:bg-surface-tint'}`}
                  onDragOver={handleDragOver}
                  onDragLeave={handleDragLeave}
                  onDrop={handleDrop}
                >
                  {image ? (
                    <div className="text-body-sm text-on-surface font-medium truncate">{image.name}</div>
                  ) : (
                    <>
                      <span className="material-symbols-outlined text-[32px] text-on-surface-variant mb-unit">cloud_upload</span>
                      <div className="font-body-md text-body-md text-on-surface">Click to upload image</div>
                      <div className="font-body-sm text-body-sm text-on-surface-variant mt-1">PNG, JPG up to 5MB</div>
                    </>
                  )}
                  <input type="file" accept="image/*" className="hidden" id="image-upload" onChange={(e) => setImage(e.target.files?.[0] || null)} />
                  <label htmlFor="image-upload" className="absolute inset-0 cursor-pointer"></label>
                </div>
                {isEdit && initialData?.image && !image && (
                  <div className="mt-stack-sm flex flex-col gap-unit">
                    <span className="font-label-sm text-label-sm text-on-surface-variant">Current Image:</span>
                    <img src={initialData.image} alt="Current product" className="w-full max-h-[160px] object-contain rounded-lg border border-outline-variant bg-surface-container-lowest p-2" />
                    <span className="text-body-xs text-on-surface-variant text-center">(Upload a new image above to replace this)</span>
                  </div>
                )}
              </div>
            </div>
          </form>
        </div>

        <div className="px-gutter py-stack-md border-t border-outline-variant bg-surface-container-lowest flex justify-end gap-stack-sm rounded-b-xl">
          <button 
            onClick={onClose}
            disabled={isSubmitting}
            className="px-stack-md h-10 border border-primary text-primary font-label-md text-label-md rounded-lg hover:bg-primary-container/10 transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-surface-container-lowest disabled:opacity-50" 
            type="button"
          >
            Cancel
          </button>
          <button 
            onClick={handleSubmit}
            disabled={isSubmitting}
            className="px-stack-md h-10 bg-primary text-on-primary font-label-md text-label-md rounded-lg hover:bg-surface-tint shadow-sm transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-surface-container-lowest flex items-center gap-2 disabled:opacity-50" 
            type="button"
          >
            {isSubmitting ? (
              <span className="material-symbols-outlined text-[18px] animate-spin">refresh</span>
            ) : (
              <span className="material-symbols-outlined text-[18px]">save</span>
            )}
            {isSubmitting ? 'Saving...' : 'Save Product'}
          </button>
        </div>
      </div>
    </div>
  );
}
