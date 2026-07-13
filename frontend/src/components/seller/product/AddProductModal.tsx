import React, { useState } from 'react';

interface AddProductModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function AddProductModal({ isOpen, onClose }: AddProductModalProps) {
  const [isDragging, setIsDragging] = useState(false);
  const [files, setFiles] = useState<File[]>([]);

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
      setFiles(Array.from(e.dataTransfer.files));
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setFiles(Array.from(e.target.files));
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-margin-mobile md:p-margin-desktop bg-on-background/30 backdrop-blur-sm overflow-y-auto">
      {/* Modal Dialog */}
      <div className="bg-surface-container-lowest border border-outline-variant rounded-xl shadow-lg w-full max-w-3xl flex flex-col relative my-auto animate-in fade-in slide-in-from-bottom-4 duration-300">
        
        {/* Header */}
        <div className="flex items-center justify-between px-gutter py-stack-md border-b border-outline-variant">
          <h2 className="font-headline-md text-headline-md text-on-surface">Add New Product</h2>
          <button 
            onClick={onClose}
            className="text-on-surface-variant hover:text-on-surface transition-colors p-unit rounded-full hover:bg-surface-container-high"
          >
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        {/* Body */}
        <div className="px-gutter py-stack-lg overflow-y-auto flex-1">
          <form className="space-y-stack-md">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-gutter">
              {/* Left Column: Details */}
              <div className="space-y-stack-md">
                {/* Product Name */}
                <div className="space-y-unit">
                  <label className="font-label-md text-label-md text-on-surface-variant" htmlFor="product-name">Product Name</label>
                  <input className="w-full h-10 px-3 border border-outline-variant rounded-lg bg-surface focus:outline-none focus:border-primary focus:ring-4 focus:ring-primary-container/20 font-body-md text-body-md text-on-surface transition-all" id="product-name" placeholder="e.g., Ergonomic Office Chair" type="text" />
                </div>

                {/* Price & Stock Row */}
                <div className="grid grid-cols-2 gap-stack-sm">
                  <div className="space-y-unit">
                    <label className="font-label-md text-label-md text-on-surface-variant" htmlFor="product-price">Price</label>
                    <div className="relative">
                      <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-on-surface-variant font-label-md">Rp</span>
                      <input className="w-full h-10 pl-10 pr-3 border border-outline-variant rounded-lg bg-surface focus:outline-none focus:border-primary focus:ring-4 focus:ring-primary-container/20 font-body-md text-body-md text-on-surface transition-all" id="product-price" min="0" placeholder="0" type="number" />
                    </div>
                  </div>
                  <div className="space-y-unit">
                    <label className="font-label-md text-label-md text-on-surface-variant" htmlFor="product-stock">Initial Stock</label>
                    <div className="relative">
                      <input className="w-full h-10 px-3 border border-outline-variant rounded-lg bg-surface focus:outline-none focus:border-primary focus:ring-4 focus:ring-primary-container/20 font-body-md text-body-md text-on-surface transition-all" id="product-stock" min="0" placeholder="0" step="1" type="number" />
                    </div>
                  </div>
                </div>

                {/* Category */}
                <div className="space-y-unit">
                  <label className="font-label-md text-label-md text-on-surface-variant" htmlFor="product-category">Category</label>
                  <div className="relative">
                    <select className="w-full h-10 px-3 pr-10 border border-outline-variant rounded-lg bg-surface focus:outline-none focus:border-primary focus:ring-4 focus:ring-primary-container/20 font-body-md text-body-md text-on-surface appearance-none transition-all" id="product-category" defaultValue="">
                      <option disabled value="">Select a category...</option>
                      <option value="electronics">Electronics &amp; Gadgets</option>
                      <option value="furniture">Home Furniture</option>
                      <option value="apparel">Clothing &amp; Apparel</option>
                      <option value="tools">Hardware &amp; Tools</option>
                    </select>
                    <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-on-surface-variant">
                      <span className="material-symbols-outlined">expand_more</span>
                    </div>
                  </div>
                </div>

                {/* Description */}
                <div className="space-y-unit h-full">
                  <label className="font-label-md text-label-md text-on-surface-variant" htmlFor="product-desc">Product Description</label>
                  <textarea className="w-full p-3 border border-outline-variant rounded-lg bg-surface focus:outline-none focus:border-primary focus:ring-4 focus:ring-primary-container/20 font-body-md text-body-md text-on-surface transition-all resize-none" id="product-desc" placeholder="Describe the product's features and benefits..." rows={5}></textarea>
                </div>
              </div>

              {/* Right Column: Image Upload */}
              <div className="space-y-stack-md flex flex-col h-full">
                <div className="space-y-unit flex-1 flex flex-col">
                  <label className="font-label-md text-label-md text-on-surface-variant">Product Images</label>
                  
                  {/* Drag and Drop Area */}
                  <div 
                    className={`flex-1 min-h-[240px] border-2 border-dashed rounded-lg transition-all flex flex-col items-center justify-center p-stack-md cursor-pointer group ${isDragging ? 'border-primary bg-primary-container/10' : 'border-outline-variant bg-surface hover:bg-surface-container-low hover:border-outline'}`}
                    onDragOver={handleDragOver}
                    onDragLeave={handleDragLeave}
                    onDrop={handleDrop}
                    onClick={() => document.getElementById('file-upload')?.click()}
                  >
                    {files.length > 0 ? (
                      <>
                        <div className="w-16 h-16 rounded-full bg-primary-container flex items-center justify-center mb-stack-sm text-on-primary-container">
                          <span className="material-symbols-outlined text-[32px]">check_circle</span>
                        </div>
                        <p className="font-label-md text-label-md text-on-surface font-semibold text-center mb-1">{files.length} file(s) ready</p>
                        <p className="font-body-sm text-body-sm text-primary text-center hover:underline">Click to change</p>
                      </>
                    ) : (
                      <>
                        <div className="w-16 h-16 rounded-full bg-surface-container-highest flex items-center justify-center mb-stack-sm text-primary group-hover:scale-110 transition-transform">
                          <span className="material-symbols-outlined text-[32px]">cloud_upload</span>
                        </div>
                        <p className="font-label-md text-label-md text-on-surface font-semibold text-center mb-1">Click to upload or drag and drop</p>
                        <p className="font-body-sm text-body-sm text-on-surface-variant text-center max-w-[200px]">SVG, PNG, JPG or GIF (MAX. 800x400px)</p>
                      </>
                    )}
                    <input accept="image/*" className="hidden" id="file-upload" multiple type="file" onChange={handleFileChange} />
                  </div>
                </div>
              </div>
            </div>
          </form>
        </div>

        {/* Footer Actions */}
        <div className="px-gutter py-stack-md border-t border-outline-variant bg-surface-container-lowest flex justify-end gap-stack-sm rounded-b-xl">
          <button 
            onClick={onClose}
            className="px-stack-md h-10 border border-primary text-primary font-label-md text-label-md rounded-lg hover:bg-primary-container/10 transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-surface-container-lowest" 
            type="button"
          >
            Cancel
          </button>
          <button 
            onClick={onClose}
            className="px-stack-md h-10 bg-primary text-on-primary font-label-md text-label-md rounded-lg hover:bg-surface-tint shadow-sm transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-surface-container-lowest flex items-center gap-2" 
            type="button"
          >
            <span className="material-symbols-outlined text-[18px]">save</span>
            Save Product
          </button>
        </div>
      </div>
    </div>
  );
}
