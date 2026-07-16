import { useEffect, useState } from 'react';
import { adminService } from '../../service/admin/admin.service';
import type { AdminCategoryResponse } from '../../service/admin/admin.service';

export function CategoryManagement() {
  const [categories, setCategories] = useState<AdminCategoryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Pagination State
  const [page, setPage] = useState(0);
  const pageSize = 5;
  
  // Filter & Sort State
  const [searchQuery, setSearchQuery] = useState('');
  const [sortMode, setSortMode] = useState('NAME');
  
  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<'CREATE' | 'EDIT'>('CREATE');
  const [currentCategory, setCurrentCategory] = useState<AdminCategoryResponse | null>(null);
  const [categoryNameInput, setCategoryNameInput] = useState('');
  
  // Delete Modal State
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const response = await adminService.getCategories();
      setCategories(response);
    } catch (error) {
      console.error("Failed to fetch categories", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const openCreateModal = () => {
    setModalMode('CREATE');
    setCurrentCategory(null);
    setCategoryNameInput('');
    setIsModalOpen(true);
  };

  const openEditModal = (category: AdminCategoryResponse) => {
    setModalMode('EDIT');
    setCurrentCategory(category);
    setCategoryNameInput(category.name);
    setIsModalOpen(true);
  };

  const openDeleteModal = (category: AdminCategoryResponse) => {
    setCurrentCategory(category);
    setIsDeleteModalOpen(true);
  };

  const handleSave = async () => {
    if (!categoryNameInput.trim()) return;

    try {
      if (modalMode === 'CREATE') {
        await adminService.createCategory({ name: categoryNameInput });
      } else if (modalMode === 'EDIT' && currentCategory) {
        await adminService.updateCategory(currentCategory.id, { name: categoryNameInput });
      }
      setIsModalOpen(false);
      fetchCategories(); // Refresh data
    } catch (error) {
      console.error("Failed to save category", error);
      alert("Failed to save category. It might already exist.");
    }
  };

  const handleDelete = async () => {
    if (!currentCategory) return;
    
    try {
      await adminService.deleteCategory(currentCategory.id);
      setIsDeleteModalOpen(false);
      fetchCategories();
    } catch (error: any) {
      console.error("Failed to delete category", error);
      alert("Cannot delete category. It might have active products assigned to it.");
    }
  };

  const filteredCategories = categories.filter(c => c.name.toLowerCase().includes(searchQuery.toLowerCase()));
  const sortedCategories = [...filteredCategories].sort((a, b) => {
    if (sortMode === 'NAME') {
      return a.name.localeCompare(b.name);
    } else if (sortMode === 'PRODUCTS') {
      return b.productCount - a.productCount; // Descending
    }
    return 0;
  });

  const totalPages = Math.ceil(sortedCategories.length / pageSize);
  const slicedCategories = sortedCategories.slice(page * pageSize, (page + 1) * pageSize);

  return (
    <section className="flex flex-col gap-4">
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-stack-sm w-full mt-stack-md mb-stack-md">
        <h2 className="font-headline-lg text-[32px] font-bold text-on-surface m-0">Categories Management</h2>
        <div className="flex gap-3">
          <div className="relative">
            <span className="material-symbols-outlined absolute left-2.5 top-2 text-outline-variant text-[18px]">search</span>
            <input 
              className="pl-9 pr-3 py-1.5 border border-outline-variant rounded text-[13px] font-mono-data focus:border-primary focus:ring-1 focus:ring-primary outline-none bg-surface-container-lowest w-64" 
              placeholder="Search categories..." 
              type="text" 
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setPage(0);
              }}
            />
          </div>
          <select 
            className="px-3 py-1.5 border border-outline-variant rounded text-[13px] font-mono-data focus:border-primary focus:ring-1 focus:ring-primary outline-none bg-surface-container-lowest"
            value={sortMode}
            onChange={(e) => {
              setSortMode(e.target.value);
              setPage(0);
            }}
          >
            <option value="NAME">Sort by: Name</option>
            <option value="PRODUCTS">Sort by: Products Count</option>
          </select>
          <button 
            onClick={openCreateModal}
            className="bg-primary text-on-primary px-4 py-1.5 rounded text-[13px] font-semibold flex items-center gap-2 hover:bg-primary-container hover:text-on-primary-container transition-colors"
          >
            <span className="material-symbols-outlined text-[14px]">add</span> Add Category
          </button>
        </div>
      </header>

      <div className="bg-surface-container-lowest border border-outline-variant rounded overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-variant border-b border-outline-variant">
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant">Category Name</th>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant text-right">Active Products</th>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant text-right">Sellers Using</th>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant text-center">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={4} className="p-stack-md text-center">
                    <div className="animate-spin inline-block w-6 h-6 border-2 border-primary border-t-transparent rounded-full"></div>
                  </td>
                </tr>
              ) : categories.length === 0 ? (
                <tr>
                  <td colSpan={4} className="p-stack-md text-center text-on-surface-variant">
                    No categories found.
                  </td>
                </tr>
              ) : (
                slicedCategories.map((category, idx) => (
                  <tr key={category.id} className={`border-b border-outline-variant hover:bg-surface-container-low transition-colors ${idx % 2 === 0 ? 'bg-surface-container-lowest' : 'bg-[#f8fafc]'}`}>
                    <td className="p-stack-sm font-body-md text-on-surface">
                      {category.name}
                    </td>
                    <td className="p-stack-sm font-mono-data text-right text-on-surface-variant">
                      {category.productCount.toLocaleString()}
                    </td>
                    <td className="p-stack-sm font-mono-data text-right text-on-surface-variant">
                      {category.sellerCount.toLocaleString()}
                    </td>
                    <td className="p-stack-sm text-center">
                      <div className="flex items-center justify-center gap-2">
                        <button
                          onClick={() => openEditModal(category)}
                          className="p-1.5 text-secondary hover:bg-secondary/10 rounded-sm transition-colors"
                          title="Edit"
                        >
                          <span className="material-symbols-outlined text-[18px]">edit</span>
                        </button>
                        <button
                          onClick={() => openDeleteModal(category)}
                          className="p-1.5 text-error hover:bg-error/10 rounded-sm transition-colors"
                          title="Delete"
                        >
                          <span className="material-symbols-outlined text-[18px]">delete</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {!loading && totalPages > 1 && (
          <div className="p-stack-sm border-t border-outline-variant flex items-center justify-between bg-surface-variant/50">
            <span className="font-label-sm text-on-surface-variant">
              Page {page + 1} of {totalPages}
            </span>
            <div className="flex gap-2">
              <button 
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}
                className="px-3 py-1 rounded-sm border border-outline-variant bg-surface-container-lowest disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface-variant font-label-md transition-colors"
              >
                Previous
              </button>
              <button 
                disabled={page >= totalPages - 1}
                onClick={() => setPage(p => p + 1)}
                className="px-3 py-1 rounded-sm border border-outline-variant bg-surface-container-lowest disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface-variant font-label-md transition-colors"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Save Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
          <div className="bg-surface-container-lowest rounded border border-outline-variant w-full max-w-sm flex flex-col shadow-lg">
            <div className="p-stack-sm border-b border-outline-variant bg-surface-variant">
              <h3 className="font-label-md text-on-surface m-0">
                {modalMode === 'CREATE' ? 'Create Category' : 'Edit Category'}
              </h3>
            </div>
            <div className="p-stack-md">
              <label className="block font-label-sm text-on-surface-variant mb-unit">Category Name</label>
              <input 
                type="text" 
                value={categoryNameInput}
                onChange={(e) => setCategoryNameInput(e.target.value)}
                className="w-full px-3 py-2 border border-outline rounded-sm font-body-md text-on-surface bg-transparent focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                placeholder="e.g. Electronics"
                autoFocus
              />
            </div>
            <div className="p-stack-sm border-t border-outline-variant flex justify-end gap-stack-sm">
              <button 
                onClick={() => setIsModalOpen(false)}
                className="px-4 py-1.5 rounded-sm font-label-sm text-on-surface-variant hover:bg-surface-variant transition-colors"
              >
                Cancel
              </button>
              <button 
                onClick={handleSave}
                disabled={!categoryNameInput.trim()}
                className="px-4 py-1.5 rounded-sm bg-primary text-on-primary font-label-sm hover:opacity-90 disabled:opacity-50 transition-opacity"
              >
                Save
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Modal */}
      {isDeleteModalOpen && currentCategory && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
          <div className="bg-surface-container-lowest rounded border border-outline-variant w-full max-w-sm flex flex-col shadow-lg">
            <div className="p-stack-sm border-b border-outline-variant bg-error/10 flex items-center gap-2">
              <span className="material-symbols-outlined text-error text-[18px]">warning</span>
              <h3 className="font-label-md text-error m-0">Delete Category</h3>
            </div>
            <div className="p-stack-md">
              <p className="font-body-md text-on-surface m-0 mb-unit">
                Are you sure you want to delete the category <strong>{currentCategory.name}</strong>?
              </p>
              {currentCategory.productCount > 0 && (
                <p className="font-label-sm text-error bg-error/10 p-2 rounded-sm border border-error/20">
                  Warning: This category currently has {currentCategory.productCount} active products. You must reassign or delete those products before this category can be removed.
                </p>
              )}
            </div>
            <div className="p-stack-sm border-t border-outline-variant flex justify-end gap-stack-sm">
              <button 
                onClick={() => setIsDeleteModalOpen(false)}
                className="px-4 py-1.5 rounded-sm font-label-sm text-on-surface-variant hover:bg-surface-variant transition-colors"
              >
                Cancel
              </button>
              <button 
                onClick={handleDelete}
                disabled={currentCategory.productCount > 0}
                className="px-4 py-1.5 rounded-sm bg-error text-on-error font-label-sm hover:opacity-90 disabled:opacity-50 transition-opacity"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
