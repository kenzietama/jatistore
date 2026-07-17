import { useState, useEffect } from 'react';
import { adminService } from '../../service/admin/admin.service';
import type { AuditTrailResponse } from '../../service/admin/admin.service';

export const AuditTrails = () => {
  const [logs, setLogs] = useState<AuditTrailResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [moduleFilter, setModuleFilter] = useState('ALL');
  const [selectedLog, setSelectedLog] = useState<AuditTrailResponse | null>(null);

  const fetchLogs = async (showLoading = true) => {
    if (showLoading) setLoading(true);
    try {
      const data = await adminService.getAuditTrails(page, 20, 'ALL', moduleFilter);
      setLogs(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (error) {
      console.error("Failed to fetch audit trails:", error);
    } finally {
      if (showLoading) setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs(true);
    
    // Auto-refresh data every 5 seconds without showing the loading spinner
    const intervalId = setInterval(() => {
      fetchLogs(false);
    }, 5000);

    return () => clearInterval(intervalId);
  }, [page, moduleFilter]);

  const formatDate = (isoString: string) => {
    const date = new Date(isoString);
    return date.toLocaleString();
  };

  const getActionColor = (action: string) => {
    if (action.includes('CREATE')) return 'border-primary/50 bg-primary/10 text-primary';
    if (action.includes('UPDATE') || action.includes('SHIPPED')) return 'border-secondary-container/50 bg-secondary-container/10 text-on-secondary-container';
    if (action.includes('DELETE')) return 'border-error/50 bg-error/10 text-error';
    if (action.includes('LOGIN') || action.includes('LOGOUT')) return 'border-tertiary/50 bg-tertiary/10 text-tertiary';
    return 'border-outline/50 bg-surface-variant text-on-surface-variant';
  };

  return (
    <section className="flex flex-col gap-4 h-full">
      {/* Top Action Bar */}
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-stack-sm w-full mt-stack-md mb-stack-md shrink-0">
        <h2 className="font-headline-lg text-[32px] font-bold text-on-surface m-0">Audit Trails</h2>
        <div className="flex items-center gap-stack-sm">
          <div className="relative">
            <select 
              value={moduleFilter}
              onChange={(e) => {
                setModuleFilter(e.target.value);
                setPage(0);
              }}
              className="appearance-none h-10 pl-3 pr-8 border border-outline-variant rounded-DEFAULT bg-surface text-label-md font-label-md text-on-surface focus:border-primary focus:ring-2 focus:ring-primary-fixed-dim/30 transition-all cursor-pointer"
            >
              <option value="ALL">All Modules</option>
              <option value="PRODUCT">Products</option>
              <option value="ORDERS">Orders</option>
              <option value="CATEGORIES">Categories</option>
              <option value="SELLERS">Sellers</option>
              <option value="AUTH">Authentication</option>
              <option value="FLASH_SALES">Flash Sales</option>
            </select>
            <span className="material-symbols-outlined absolute right-2 top-1/2 -translate-y-1/2 text-outline pointer-events-none text-[18px]">arrow_drop_down</span>
          </div>
        </div>
      </header>

      {/* Technical Data Table Area */}
      <div className="bg-surface-container-lowest border border-outline-variant rounded overflow-hidden flex flex-col flex-1 min-h-[400px]">
        <div className="overflow-x-auto flex-1 relative">
          <table className="w-full text-left border-collapse">
            <thead className="sticky top-0 bg-surface-variant border-b border-outline-variant z-10 shadow-sm">
              <tr>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant whitespace-nowrap w-48">Timestamp</th>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant whitespace-nowrap w-32">User</th>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant whitespace-nowrap w-24">Role</th>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant whitespace-nowrap w-32">Action</th>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant whitespace-nowrap w-full">Description</th>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant whitespace-nowrap w-20 text-center">Action</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={6} className="p-stack-md text-center text-on-surface-variant">
                    <div className="animate-spin inline-block w-6 h-6 border-2 border-primary border-t-transparent rounded-full"></div>
                  </td>
                </tr>
              ) : logs.length === 0 ? (
                <tr>
                  <td colSpan={6} className="p-stack-md text-center text-on-surface-variant">
                    No audit trails found.
                  </td>
                </tr>
              ) : (
                logs.map((log, index) => (
                  <tr key={log.id} className={`border-b border-outline-variant hover:bg-surface-container-low transition-colors group ${index % 2 === 0 ? 'bg-surface-container-lowest' : 'bg-[#f8fafc]'}`}>
                    <td className="p-stack-sm font-mono-data text-[13px] text-on-surface-variant">{formatDate(log.createdAt)}</td>
                    <td className="p-stack-sm text-secondary font-mono-data text-[13px]">
                      {log.username || log.userId || 'SYSTEM'}
                    </td>
                    <td className="p-stack-sm text-on-surface-variant text-[13px] font-semibold">
                      {log.userRole || '-'}
                    </td>
                    <td className="p-stack-sm">
                      <span className={`inline-flex items-center px-2 py-0.5 rounded-sm border font-label-sm text-[10px] uppercase font-bold tracking-widest ${getActionColor(log.action)}`}>
                        {log.action}
                      </span>
                    </td>
                    <td className="p-stack-sm text-on-surface-variant text-[13px]">
                      <div className="truncate max-w-[200px] md:max-w-[300px] lg:max-w-[400px] xl:max-w-[600px] block" title={log.description}>
                        {log.description}
                      </div>
                    </td>
                    <td className="p-stack-sm text-center">
                      <button 
                        onClick={() => setSelectedLog(log)}
                        className="p-1.5 rounded-full text-on-surface-variant hover:bg-surface-container-high hover:text-primary transition-colors"
                        title="View Details"
                      >
                        <span className="material-symbols-outlined text-[18px]">visibility</span>
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        <div className="p-stack-sm border-t border-outline-variant flex items-center justify-between bg-surface-variant/50 shrink-0">
          <p className="font-label-sm text-on-surface-variant">
            Showing {totalElements === 0 ? 0 : page * 20 + 1}-{Math.min((page + 1) * 20, totalElements)} of {totalElements}
          </p>
          <div className="flex gap-2">
            <button 
              disabled={page === 0}
              onClick={() => setPage(p => Math.max(0, p - 1))}
              className="px-3 py-1 bg-surface border border-outline-variant rounded-sm text-on-surface-variant hover:bg-surface-container disabled:opacity-50 transition-colors font-label-sm"
            >
              Previous
            </button>
            <button 
              disabled={page >= totalPages - 1}
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
              className="px-3 py-1 bg-surface border border-outline-variant rounded-sm text-on-surface-variant hover:bg-surface-container disabled:opacity-50 transition-colors font-label-sm"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {/* Modal */}
      {selectedLog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <div className="bg-surface rounded-xl shadow-lg w-full max-w-2xl max-h-[90vh] flex flex-col overflow-hidden border border-outline-variant/30">
            <div className="p-4 border-b border-outline-variant/50 flex items-center justify-between bg-surface-container-low">
              <h3 className="font-title-lg text-title-lg text-on-surface flex items-center gap-2">
                <span className="material-symbols-outlined text-primary">policy</span>
                Audit Trail Details
              </h3>
              <button 
                onClick={() => setSelectedLog(null)}
                className="p-2 rounded-full text-on-surface-variant hover:bg-surface-container-high hover:text-error transition-colors"
              >
                <span className="material-symbols-outlined text-[20px]">close</span>
              </button>
            </div>
            
            <div className="p-6 overflow-y-auto flex-1 font-body-md text-on-surface-variant space-y-6">
              <div className="grid grid-cols-2 gap-y-6 gap-x-4">
                <div>
                  <p className="text-[11px] font-semibold text-outline uppercase tracking-wider mb-1">Log ID</p>
                  <p className="font-mono-data text-[13px]">{selectedLog.id}</p>
                </div>
                <div>
                  <p className="text-[11px] font-semibold text-outline uppercase tracking-wider mb-1">Timestamp</p>
                  <p className="font-mono-data text-[13px]">{formatDate(selectedLog.createdAt)}</p>
                </div>
                
                <div>
                  <p className="text-[11px] font-semibold text-outline uppercase tracking-wider mb-1">User</p>
                  <p className="font-semibold text-secondary text-sm">{selectedLog.username || selectedLog.userId || 'SYSTEM'}</p>
                </div>
                <div>
                  <p className="text-[11px] font-semibold text-outline uppercase tracking-wider mb-1">Role</p>
                  <p className="text-sm font-semibold">{selectedLog.userRole || '-'}</p>
                </div>
                
                <div>
                  <p className="text-[11px] font-semibold text-outline uppercase tracking-wider mb-1">Action</p>
                  <span className={`inline-flex items-center px-2 py-0.5 rounded-sm border font-label-sm text-[10px] uppercase font-bold tracking-widest ${getActionColor(selectedLog.action)}`}>
                    {selectedLog.action}
                  </span>
                </div>
                <div>
                  <p className="text-[11px] font-semibold text-outline uppercase tracking-wider mb-1">Module</p>
                  <p className="text-sm font-semibold">{selectedLog.affectedModule || '-'}</p>
                </div>
                
                <div>
                  <p className="text-[11px] font-semibold text-outline uppercase tracking-wider mb-1">Entity ID</p>
                  <p className="font-mono-data text-[13px]">{selectedLog.entityId || '-'}</p>
                </div>
                <div>
                  <p className="text-[11px] font-semibold text-outline uppercase tracking-wider mb-1">IP Address</p>
                  <p className="font-mono-data text-[13px]">{selectedLog.ipAddress || 'Unknown'}</p>
                </div>
              </div>
              
              <div>
                <p className="text-[11px] font-semibold text-outline uppercase tracking-wider mb-1">Description</p>
                <div className="p-3 bg-surface-container-lowest border border-outline-variant/30 rounded-lg text-on-surface text-sm">
                  {selectedLog.description}
                </div>
              </div>

              <div>
                <p className="text-[11px] font-semibold text-outline uppercase tracking-wider mb-1">Payload</p>
                <div className="p-3 bg-surface-container-lowest border border-outline-variant/30 rounded-lg overflow-x-auto max-h-[300px]">
                  <pre className="font-mono-data text-[12px] text-on-surface whitespace-pre-wrap break-all">
                    {(() => {
                      if (!selectedLog.payload) return 'No payload recorded.';
                      try {
                        const parsed = typeof selectedLog.payload === 'string' 
                          ? JSON.parse(selectedLog.payload) 
                          : selectedLog.payload;
                        return JSON.stringify(parsed, null, 2);
                      } catch (e) {
                        return String(selectedLog.payload);
                      }
                    })()}
                  </pre>
                </div>
              </div>
            </div>

            <div className="p-4 border-t border-outline-variant/50 bg-surface-container-low flex justify-end">
              <button 
                onClick={() => setSelectedLog(null)}
                className="px-6 py-2 bg-primary text-on-primary rounded-full font-label-lg hover:bg-primary/90 transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

    </section>
  );
};
