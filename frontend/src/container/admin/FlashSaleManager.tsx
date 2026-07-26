import React, { useState, useEffect } from "react";
import {
  adminFlashSaleService,
  type FlashSaleRequest,
  type FlashSaleResponse,
} from "../../service/admin/flash-sale.service";

export const FlashSaleManager: React.FC = () => {
  const [flashSales, setFlashSales] = useState<FlashSaleResponse[]>([]);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [sortBy, setSortBy] = useState("startTime");
  const [sortDir, setSortDir] = useState("desc");

  const [formData, setFormData] = useState({
    name: "",
    startDate: "",
    startTime: "12:00",
    endDate: "",
    endTime: "23:59",
  });
  const [editingId, setEditingId] = useState<string | null>(null);

  const [deleteTargetId, setDeleteTargetId] = useState<string | null>(null);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isSaveModalOpen, setIsSaveModalOpen] = useState(false);
  const [saveRequestData, setSaveRequestData] = useState<FlashSaleRequest | null>(null);
  const [errorModalMsg, setErrorModalMsg] = useState<string | null>(null);

  const fetchFlashSales = async () => {
    try {
      const res = await adminFlashSaleService.getFlashSales(page, 10, statusFilter, searchQuery, sortBy, sortDir);
      setFlashSales(res.data.content);
      setTotalPages(res.data.totalPages);
    } catch (error) {
      console.error("Error fetching flash sales:", error);
    }
  };

  useEffect(() => {
    fetchFlashSales();
  }, [page, statusFilter, searchQuery, sortBy, sortDir]);

  const handleSort = (field: string) => {
    if (sortBy === field) {
      setSortDir(sortDir === "asc" ? "desc" : "asc");
    } else {
      setSortBy(field);
      setSortDir("asc");
    }
  };

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage(null);
    }, 3000);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target;
    setFormData((prev) => ({ ...prev, [id]: value }));
  };

  const handleTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target;
    let digits = value.replace(/\D/g, "");
    if (digits.length > 2) {
      digits = digits.substring(0, 2) + ":" + digits.substring(2, 4);
    }
    setFormData((prev) => ({ ...prev, [id]: digits }));
  };

  const handleClear = () => {
    setFormData({
      name: "",
      startDate: "",
      startTime: "12:00",
      endDate: "",
      endTime: "23:59",
    });
    setEditingId(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const startDateTime = new Date(`${formData.startDate}T${formData.startTime}:00`);
    const endDateTime = new Date(`${formData.endDate}T${formData.endTime}:00`);

    if (!editingId && startDateTime < new Date()) {
      setErrorModalMsg("Start time cannot be in the past.");
      return;
    }

    if (endDateTime <= startDateTime) {
      setErrorModalMsg("End time must be after start time.");
      return;
    }

    const request: FlashSaleRequest = {
      name: formData.name,
      startTime: startDateTime.toISOString(),
      endTime: endDateTime.toISOString(),
    };

    setSaveRequestData(request);
    setIsSaveModalOpen(true);
  };

  const confirmSave = async () => {
    if (!saveRequestData) return;
    try {
      if (editingId) {
        await adminFlashSaleService.updateFlashSale(editingId, saveRequestData);
        showToast("Flash sale updated successfully.");
      } else {
        await adminFlashSaleService.createFlashSale(saveRequestData);
        showToast("Flash sale created successfully.");
      }
      handleClear();
      fetchFlashSales();
      setIsSaveModalOpen(false);
    } catch (error: any) {
      console.error("Error saving flash sale:", error);
      setErrorModalMsg(error.response?.data?.restApiResponseMessage || error.response?.data?.message || "Failed to save flash sale");
      setIsSaveModalOpen(false);
    }
  };

  const handleEdit = (fs: FlashSaleResponse) => {
    if (fs.status === "ENDED") {
      setErrorModalMsg("Ended flash sale cannot be edited.");
      return;
    }

    const sd = new Date(fs.startTime);
    const ed = new Date(fs.endTime);

    const formatLocal = (d: Date) => {
      const date = d.getFullYear() + "-" + String(d.getMonth() + 1).padStart(2, '0') + "-" + String(d.getDate()).padStart(2, '0');
      const time = String(d.getHours()).padStart(2, '0') + ":" + String(d.getMinutes()).padStart(2, '0');
      return { date, time };
    };

    const start = formatLocal(sd);
    const end = formatLocal(ed);

    setFormData({
      name: fs.name,
      startDate: start.date,
      startTime: start.time,
      endDate: end.date,
      endTime: end.time,
    });
    setEditingId(fs.id);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleDelete = (id: string) => {
    setDeleteTargetId(id);
    setIsDeleteModalOpen(true);
  };

  const confirmDelete = async () => {
    if (!deleteTargetId) return;
    try {
      await adminFlashSaleService.deleteFlashSale(deleteTargetId);
      showToast("Flash sale deleted successfully.");
      fetchFlashSales();
      setIsDeleteModalOpen(false);
      setDeleteTargetId(null);
    } catch (error: any) {
      console.error("Error deleting flash sale:", error);
      setErrorModalMsg(error.response?.data?.message || "Failed to delete flash sale");
      setIsDeleteModalOpen(false);
      setDeleteTargetId(null);
    }
  };

  const getStatusChip = (status: string) => {
    switch (status) {
      case "UPCOMING":
        return <span className="px-2 py-0.5 rounded-full bg-primary-container/10 text-primary font-label-sm">Upcoming</span>;
      case "ACTIVE":
        return <span className="px-2 py-0.5 rounded-full bg-success/10 text-success font-label-sm">Active</span>;
      case "ENDED":
        return <span className="px-2 py-0.5 rounded-full bg-surface-container-highest text-on-surface-variant font-label-sm">Ended</span>;
      default:
        return <span>{status}</span>;
    }
  };

  return (
    <section className="flex flex-col gap-4 h-full relative">
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-stack-sm w-full mt-stack-md mb-stack-md shrink-0">
        <h2 className="font-headline-lg text-[32px] font-bold text-on-surface m-0">Flash Sale Configuration</h2>
      </header>

      <div className="w-full pb-stack-lg flex flex-col gap-stack-lg">
        {/* Configuration Card */}
        <div className="bg-surface border border-outline-variant rounded-lg shadow-sm">
          <div className="p-stack-md border-b border-outline-variant bg-surface-container-low rounded-t-lg">
            <h2 className="font-headline-sm text-headline-sm text-on-surface flex items-center gap-stack-sm">
              <span className="material-symbols-outlined text-primary">schedule</span>
              Schedule Window
            </h2>
          </div>
          <div className="p-gutter">
            <form className="space-y-gutter" onSubmit={handleSubmit}>
              <div className="space-y-stack-sm">
                <label className="block font-label-md text-label-md text-on-surface" htmlFor="name">Flash Sale Name</label>
                <div className="relative">
                  <input
                    className="w-full h-10 px-3 bg-surface border border-outline rounded text-on-surface font-body-md focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all outline-none"
                    id="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    placeholder="e.g. Mega Midnight Sale"
                    required
                    maxLength={50}
                    type="text"
                  />
                </div>
              </div>

              {/* Start Date/Time */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-stack-md">
                <div className="space-y-stack-sm">
                  <label className="block font-label-md text-label-md text-on-surface" htmlFor="startDate">Start Date</label>
                  <div className="relative">
                    <input
                      className="w-full h-10 px-3 bg-surface border border-outline rounded text-on-surface font-body-md focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all outline-none"
                      id="startDate"
                      value={formData.startDate}
                      onChange={handleInputChange}
                      min={new Date().toLocaleDateString('en-CA')}
                      required
                      type="date"
                    />
                  </div>
                </div>
                <div className="space-y-stack-sm">
                  <label className="block font-label-md text-label-md text-on-surface" htmlFor="startTime">Start Time</label>
                  <div className="relative">
                    <input
                      className="w-full h-10 px-3 bg-surface border border-outline rounded text-on-surface font-body-md focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all outline-none"
                      id="startTime"
                      value={formData.startTime}
                      onChange={handleTimeChange}
                      required
                      type="text"
                      pattern="([01]?[0-9]|2[0-3]):[0-5][0-9]"
                      placeholder="HH:mm (24h)"
                      maxLength={5}
                    />
                  </div>
                </div>
              </div>

              {/* End Date/Time */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-stack-md">
                <div className="space-y-stack-sm">
                  <label className="block font-label-md text-label-md text-on-surface" htmlFor="endDate">End Date</label>
                  <div className="relative">
                    <input
                      className="w-full h-10 px-3 bg-surface border border-outline rounded text-on-surface font-body-md focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all outline-none"
                      id="endDate"
                      value={formData.endDate}
                      onChange={handleInputChange}
                      min={formData.startDate || new Date().toLocaleDateString('en-CA')}
                      required
                      type="date"
                    />
                  </div>
                </div>
                <div className="space-y-stack-sm">
                  <label className="block font-label-md text-label-md text-on-surface" htmlFor="endTime">End Time</label>
                  <div className="relative">
                    <input
                      className="w-full h-10 px-3 bg-surface border border-outline rounded text-on-surface font-body-md focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all outline-none"
                      id="endTime"
                      value={formData.endTime}
                      onChange={handleTimeChange}
                      required
                      type="text"
                      pattern="([01]?[0-9]|2[0-3]):[0-5][0-9]"
                      placeholder="HH:mm (24h)"
                      maxLength={5}
                    />
                  </div>
                </div>
              </div>

              {/* Visual Spacer */}
              <div className="h-px w-full bg-outline-variant my-stack-md"></div>

              {/* Actions */}
              <div className="flex items-center justify-end gap-stack-md pt-stack-sm">
                <button
                  type="button"
                  onClick={handleClear}
                  className="px-4 h-10 font-label-md text-label-md font-mono-data text-on-surface-variant hover:bg-surface-container-highest rounded border border-outline-variant transition-colors flex items-center justify-center"
                >
                  Clear Form
                </button>
                <button
                  type="submit"
                  className="px-6 h-10 font-label-md text-label-md font-mono-data bg-primary text-on-primary hover:bg-surface-tint rounded transition-colors flex items-center justify-center shadow-sm"
                >
                  {editingId ? "Update Window" : "Set Flash Sale Window"}
                </button>
              </div>
            </form>
          </div>
        </div>

        {/* Table List */}
        <div className="bg-surface border border-outline-variant rounded-lg shadow-sm overflow-hidden">
          <div className="p-stack-md border-b border-outline-variant bg-surface-container-low flex flex-col sm:flex-row gap-stack-sm justify-between items-start sm:items-center">
            <h2 className="font-headline-sm text-headline-sm text-on-surface flex items-center gap-stack-sm shrink-0">
              <span className="material-symbols-outlined text-primary">list_alt</span>Active & Upcoming Flash Sales
            </h2>
            <div className="flex flex-col sm:flex-row items-center gap-stack-sm w-full sm:w-auto">
              <input type="text" placeholder="Search event name..." value={searchQuery} onChange={(e) => {setSearchQuery(e.target.value); setPage(0);}} className="w-full sm:w-64 h-10 px-3 bg-surface border border-outline rounded text-on-surface font-body-sm focus:border-primary outline-none" />
              <div className="relative w-full sm:w-auto">
                <select value={statusFilter} onChange={(e) => {setStatusFilter(e.target.value); setPage(0);}} className="w-full sm:w-auto appearance-none h-10 pl-3 pr-8 border border-outline rounded bg-surface text-on-surface font-body-sm focus:border-primary outline-none cursor-pointer">
                  <option value="">All Status</option>
                  <option value="UPCOMING">Upcoming</option>
                  <option value="ACTIVE">Active</option>
                  <option value="ENDED">Ended</option>
                </select>
                <span className="material-symbols-outlined absolute right-2 top-1/2 -translate-y-1/2 text-outline pointer-events-none text-[18px]">arrow_drop_down</span>
              </div>
            </div>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead className="bg-surface-container-low border-b border-outline-variant">
                <tr className="font-label-md text-label-md text-on-surface-variant">
                  <th className="p-stack-md cursor-pointer hover:bg-surface-container-highest transition-colors" onClick={() => handleSort('name')}>Event Name {sortBy === 'name' ? (sortDir === 'asc' ? '↑' : '↓') : ''}</th>
                  <th className="p-stack-md cursor-pointer hover:bg-surface-container-highest transition-colors" onClick={() => handleSort('startTime')}>Start Date {sortBy === 'startTime' ? (sortDir === 'asc' ? '↑' : '↓') : ''}</th>
                  <th className="p-stack-md cursor-pointer hover:bg-surface-container-highest transition-colors" onClick={() => handleSort('endTime')}>End Date {sortBy === 'endTime' ? (sortDir === 'asc' ? '↑' : '↓') : ''}</th>
                  <th className="p-stack-md">Status</th>
                  <th className="p-stack-md text-center">Actions</th>
                </tr>
              </thead>
              <tbody className="font-body-sm text-on-surface">
                {flashSales.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="p-stack-md text-center text-on-surface-variant">
                      No flash sales found
                    </td>
                  </tr>
                ) : (
                  flashSales.map((fs) => (
                    <tr key={fs.id} className="border-b border-outline-variant/50 hover:bg-surface-container-low transition-colors">
                      <td className="p-stack-md font-bold">{fs.name}</td>
                      <td className="p-stack-md">{new Date(fs.startTime).toLocaleString("en-GB", { year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit', hourCycle: 'h23' })}</td>
                      <td className="p-stack-md">{new Date(fs.endTime).toLocaleString("en-GB", { year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit', hourCycle: 'h23' })}</td>
                      <td className="p-stack-md">{getStatusChip(fs.status)}</td>
                       <td className="p-stack-md flex justify-center gap-stack-sm">
                        {fs.status !== 'ENDED' ? (
                          <button onClick={() => handleEdit(fs)} className="text-on-surface-variant hover:text-primary" title="Edit Flash Sale">
                            <span className="material-symbols-outlined text-[20px]">edit</span>
                          </button>
                        ) : (
                          <button disabled className="text-outline-variant cursor-not-allowed opacity-50" title="Ended flash sale cannot be edited">
                            <span className="material-symbols-outlined text-[20px]">edit</span>
                          </button>
                        )}
                        <button onClick={() => handleDelete(fs.id)} className="text-on-surface-variant hover:text-error">
                          <span className="material-symbols-outlined text-[20px]">delete</span>
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          {totalPages > 1 && (
            <div className="p-stack-sm border-t border-outline-variant bg-surface flex items-center justify-between">
              <button disabled={page === 0} onClick={() => setPage(page - 1)} className="px-4 h-9 font-label-md text-on-surface hover:bg-surface-container-highest rounded disabled:opacity-50 transition-colors border border-outline-variant">Previous</button>
              <span className="font-body-sm text-on-surface-variant">Page {page + 1} of {totalPages}</span>
              <button disabled={page === totalPages - 1} onClick={() => setPage(page + 1)} className="px-4 h-9 font-label-md text-on-surface hover:bg-surface-container-highest rounded disabled:opacity-50 transition-colors border border-outline-variant">Next</button>
            </div>
          )}
        </div>
      </div>

      {/* Toast Notification */}
      <div
        className={`fixed bottom-gutter right-gutter bg-inverse-surface text-inverse-on-surface px-stack-md py-stack-sm rounded-lg shadow-lg flex items-center gap-stack-sm transform transition-transform duration-300 z-50 pointer-events-none ${toastMessage ? "translate-y-0" : "translate-y-[150%]"}`}
      >
        <span className="material-symbols-outlined text-primary-fixed">check_circle</span>
        <span className="font-body-sm text-body-sm">{toastMessage}</span>
      </div>
      {/* Delete Confirmation Modal */}
      {isDeleteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-gutter bg-scrim/40 backdrop-blur-sm">
          <div className="bg-surface rounded-xl shadow-lg w-full max-w-md overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <div className="p-stack-lg border-b border-outline-variant flex items-center gap-stack-sm">
              <span className="material-symbols-outlined text-error">warning</span>
              <h3 className="font-title-md text-title-md text-on-surface m-0">Confirm Deletion</h3>
            </div>
            <div className="p-stack-lg bg-surface-container-lowest">
              <p className="font-body-md text-on-surface-variant m-0">
                Are you sure you want to delete this Flash Sale? This action cannot be undone and is only allowed if no products are attached.
              </p>
            </div>
            <div className="p-stack-md bg-surface-container-low flex justify-end gap-stack-sm border-t border-outline-variant">
              <button
                type="button"
                className="px-4 h-10 font-label-md text-label-md font-mono-data text-on-surface-variant hover:bg-surface-container-highest rounded border border-outline-variant transition-colors flex items-center justify-center"
                onClick={() => setIsDeleteModalOpen(false)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="px-6 h-10 font-label-md text-label-md font-mono-data bg-error text-error-on hover:bg-error/90 rounded transition-colors flex items-center justify-center shadow-sm"
                onClick={confirmDelete}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Save Confirmation Modal */}
      {isSaveModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-gutter bg-scrim/40 backdrop-blur-sm">
          <div className="bg-surface rounded-xl shadow-lg w-full max-w-md overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <div className="p-stack-lg border-b border-outline-variant flex items-center gap-stack-sm">
              <span className="material-symbols-outlined text-primary">save</span>
              <h3 className="font-title-md text-title-md text-on-surface m-0">Confirm Save</h3>
            </div>
            <div className="p-stack-lg bg-surface-container-lowest">
              <p className="font-body-md text-on-surface-variant m-0">
                Are you sure you want to {editingId ? "update this Flash Sale event" : "create a new Flash Sale event"}?
              </p>
            </div>
            <div className="p-stack-md bg-surface-container-low flex justify-end gap-stack-sm border-t border-outline-variant">
              <button
                type="button"
                className="px-4 h-10 font-label-md text-label-md font-mono-data text-on-surface-variant hover:bg-surface-container-highest rounded border border-outline-variant transition-colors flex items-center justify-center"
                onClick={() => setIsSaveModalOpen(false)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="px-6 h-10 font-label-md text-label-md font-mono-data bg-primary text-on-primary hover:bg-surface-tint rounded transition-colors flex items-center justify-center shadow-sm"
                onClick={confirmSave}
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Error/Validation Modal */}
      {errorModalMsg && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-gutter bg-scrim/40 backdrop-blur-sm">
          <div className="bg-surface rounded-xl shadow-lg w-full max-w-md overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <div className="p-stack-lg border-b border-outline-variant flex items-center gap-stack-sm">
              <span className="material-symbols-outlined text-error">error</span>
              <h3 className="font-title-md text-title-md text-on-surface m-0">Validation Error</h3>
            </div>
            <div className="p-stack-lg bg-surface-container-lowest">
              <p className="font-body-md text-on-surface-variant m-0 text-center">
                {errorModalMsg}
              </p>
            </div>
            <div className="p-stack-md bg-surface-container-low flex justify-end border-t border-outline-variant">
              <button
                type="button"
                className="px-6 h-10 font-label-md text-label-md font-mono-data bg-primary text-on-primary hover:bg-surface-tint rounded transition-colors flex items-center justify-center shadow-sm"
                onClick={() => setErrorModalMsg(null)}
              >
                Got it
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
};
