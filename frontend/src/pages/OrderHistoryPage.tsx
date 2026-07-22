import React, { useState, useEffect } from "react";
import api from "../lib/api";
import { useNavigate } from "react-router-dom";

interface OrderItem {
	productName: string;
	quantity: number;
	pricePerItem: number;
	isFlashSale: boolean;
	imageUrl?: string;
}

interface Order {
	orderId: string;
	orderDate: string;
	totalAmount: number;
	status: "PENDING" | "PAID_ON_HOLD" | "SHIPPED" | "RECEIVED" | "CANCELLED";
	items: OrderItem[];
}

const OrderHistoryPage: React.FC = () => {
	const navigate = useNavigate();
	const [orders, setOrders] = useState<Order[]>([]);
	const [isLoading, setIsLoading] = useState(true);
	const [statusFilter, setStatusFilter] = useState<string>("ALL");
	const [filterOpen, setFilterOpen] = useState<boolean>(false);
	const [searchQuery, setSearchQuery] = useState("");

	const [processingOrderId, setProcessingOrderId] = useState<string | null>(null);
	const [showToast, setShowToast] = useState(false);
	const [toastAnimationClass, setToastAnimationClass] = useState("toast-enter");
	const [toastMessage, setToastMessage] = useState("");
	
	const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);

	useEffect(() => {
		fetchOrders();
	}, []);

	const fetchOrders = async () => {
		try {
			setIsLoading(true);
			const token = localStorage.getItem("jatistore_token");

			if (!token) {
				navigate("/auth/login");
				return;
			}

			const response = await api.get("/api/v1/orders");

			if (response.data && (response.data.code === 200 || response.data.restApiResponseHttpCode === 200)) {
				const responseData = response.data.data || response.data.restApiResponseData;
				const orderData = responseData?.content || [];
				setOrders(orderData);
			}
		} catch (error) {
			console.error("Failed to fetch orders:", error);
		} finally {
			setIsLoading(false);
		}
	};

	const handleReceiveOrder = async (orderId: string) => {
		setProcessingOrderId(orderId);

		try {
			const token = localStorage.getItem("jatistore_token");

			if (!token) {
				navigate("/auth/login");
				return;
			}

			const response = await api.post(`/api/v1/orders/${orderId}/confirm-receipt`);

			if (response.data && (response.data.code === 200 || response.data.restApiResponseHttpCode === 200)) {
				setOrders((prevOrders) =>
					prevOrders.map((order) =>
						order.orderId === orderId
							? { ...order, status: "RECEIVED" }
							: order,
					),
				);
				setToastMessage("Order receipt confirmed. Funds released to seller.");
				triggerToast();
			}
		} catch (error) {
			console.error("Failed to confirm receipt:", error);
			setToastMessage("Failed to confirm receipt. Please try again.");
			triggerToast();
		} finally {
			setProcessingOrderId(null);
		}
	};

	const triggerToast = () => {
		setShowToast(true);
		setToastAnimationClass("toast-enter");

		const timer = setTimeout(() => {
			handleCloseToast();
		}, 5000);

		return () => clearTimeout(timer);
	};

	const handleCloseToast = () => {
		setToastAnimationClass("toast-exit");
		setTimeout(() => {
			setShowToast(false);
		}, 300);
	};

	const formatDate = (isoDate: string) => {
		const date = new Date(isoDate);
		return date.toLocaleDateString("en-US", {
			year: "numeric",
			month: "short",
			day: "numeric",
		});
	};

	const getStatusDisplay = (status: string) => {
		switch (status) {
			case "PENDING":
				return {
					label: "Pending",
					icon: "hourglass_empty",
					class: "bg-tertiary-container/20 border-tertiary text-tertiary",
				};
			case "PAID_ON_HOLD":
				return {
					label: "Processing",
					icon: "schedule",
					class: "bg-tertiary-container/20 border-tertiary text-tertiary",
				};
			case "SHIPPED":
				return {
					label: "Shipped",
					icon: "local_shipping",
					class: "bg-secondary-container/20 border-secondary text-on-secondary-container",
				};
			case "RECEIVED":
				return {
					label: "Received",
					icon: "task_alt",
					class: "bg-primary-container/10 border-primary text-primary",
				};
			case "CANCELLED":
				return {
					label: "Cancelled",
					icon: "cancel",
					class: "bg-error-container/20 border-error text-error",
				};
			default:
				return {
					label: status,
					icon: "info",
					class: "bg-surface-container border-outline text-on-surface",
				};
		}
	};

	const filteredOrders = orders.filter((order) => {
		const matchesStatus = statusFilter === "ALL" || order.status === statusFilter;
		const matchesSearch =
			order.items.some((item) =>
				item.productName.toLowerCase().includes(searchQuery.toLowerCase()),
			) ||
			order.orderId.toLowerCase().includes(searchQuery.toLowerCase());

		return matchesStatus && matchesSearch;
	});

	return (
		<div className="space-y-gutter relative h-full flex flex-col">
			{/* Horizontal Filters Section */}
			<section className="bg-surface-container-lowest border border-outline-variant rounded-xl p-stack-sm shadow-sm flex flex-col sm:flex-row items-center gap-4 justify-between">
				<div className="w-full sm:w-64 flex items-center relative shrink-0">
					<span className="material-symbols-outlined absolute left-3 text-on-surface-variant text-[18px]">
						search
					</span>
					<input
						type="text"
						value={searchQuery}
						onChange={(e) => setSearchQuery(e.target.value)}
						placeholder="Search product or ID..."
						className="w-full bg-surface border border-outline-variant rounded-full pl-9 pr-4 py-2 text-body-sm focus:ring-1 focus:ring-primary focus:border-primary outline-none transition-all"
					/>
				</div>
				
				<div className="flex-shrink-0 w-full sm:w-auto flex items-center gap-2">
					<div className="relative">
						<button 
							onClick={() => setFilterOpen(!filterOpen)}
							className="flex items-center gap-2 px-4 py-2 bg-surface border border-outline-variant rounded-lg font-label-md text-on-surface hover:bg-surface-container transition-colors"
						>
							<span className="material-symbols-outlined text-[18px]">filter_list</span>
							Filters {statusFilter !== 'ALL' && <span className="w-2 h-2 rounded-full bg-primary"></span>}
						</button>
						
						{filterOpen && (
							<div className="absolute right-0 mt-2 w-48 bg-surface border border-outline-variant rounded-lg shadow-lg z-50 py-2">
								<div className="px-4 py-2 text-label-sm text-on-surface-variant uppercase tracking-wider">Filter Status</div>
								{['ALL', 'PENDING', 'PAID_ON_HOLD', 'SHIPPED', 'RECEIVED', 'CANCELLED'].map((s) => (
									<button
										key={s}
										className={`w-full text-left px-4 py-2 font-body-sm hover:bg-surface-container transition-colors ${
											statusFilter === s ? 'text-primary bg-primary-container/10 font-medium' : 'text-on-surface'
										}`}
										onClick={() => { setStatusFilter(s); setFilterOpen(false); }}
									>
										{s === 'ALL' ? 'All Orders' : s === 'PAID_ON_HOLD' ? 'Processing' : s.charAt(0) + s.slice(1).toLowerCase()}
									</button>
								))}
							</div>
						)}
					</div>
				</div>
			</section>

			{/* Order List Content */}
			<div className="flex-1 space-y-stack-md">
				{isLoading ? (
					<div className="text-center py-12 font-body-md text-on-surface-variant">
						Loading orders...
					</div>
				) : filteredOrders.length > 0 ? (
					filteredOrders.map((order) => {
						const statusInfo = getStatusDisplay(order.status);
						return (
							<article
								key={order.orderId}
								className="bg-surface-container-lowest border border-outline-variant rounded-xl overflow-hidden hover:shadow-md transition-shadow duration-300"
							>
								<div className="bg-surface-container-low px-stack-md py-stack-sm flex flex-wrap items-center justify-between border-b border-outline-variant gap-4">
									<div className="flex flex-wrap gap-x-stack-lg gap-y-2">
										<div>
											<p className="text-[10px] text-on-surface-variant uppercase font-bold">Order #</p>
											<p className="font-mono-data text-on-surface">{order.orderId}</p>
										</div>
										<div>
											<p className="text-[10px] text-on-surface-variant uppercase font-bold">Date</p>
											<p className="font-body-sm text-on-surface">{formatDate(order.orderDate)}</p>
										</div>
										<div>
											<p className="text-[10px] text-on-surface-variant uppercase font-bold">Total</p>
											<p className="font-body-sm font-semibold text-primary">Rp {order.totalAmount.toLocaleString("id-ID")}</p>
										</div>
									</div>
									<span className={`px-3 py-1 rounded-full text-label-sm font-bold flex items-center gap-1 border ${statusInfo.class}`}>
										<span className="material-symbols-outlined text-[16px]">{statusInfo.icon}</span>
										{statusInfo.label.toUpperCase()}
									</span>
								</div>

								<div className="p-stack-md space-y-4">
									{order.items.map((item, idx) => (
										<div key={idx} className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
											<div className="flex items-center gap-4 flex-1">
												<div className="w-16 h-16 bg-surface-container-high rounded-lg flex items-center justify-center shrink-0 overflow-hidden border border-outline-variant">
													{item.imageUrl ? (
														<img src={item.imageUrl} alt={item.productName} className="w-full h-full object-cover" />
													) : (
														<span className="material-symbols-outlined text-outline">inventory_2</span>
													)}
												</div>
												<div>
													<h4 className="font-label-md text-on-surface text-base">
														{item.productName}
														{item.isFlashSale && (
															<span className="ml-2 text-[10px] text-error bg-error-container/30 px-2 py-0.5 rounded-full">
																⚡ Flash Sale
															</span>
														)}
													</h4>
													<p className="text-body-sm font-medium mt-1">Qty: {item.quantity}</p>
												</div>
											</div>
											<div className="sm:text-right w-full sm:w-auto flex sm:flex-col justify-between items-center sm:items-end">
												<p className="text-[10px] text-on-surface-variant uppercase">Subtotal</p>
												<p className="text-body-md font-mono-data text-on-surface font-semibold">
													Rp {(item.pricePerItem * item.quantity).toLocaleString("id-ID")}
												</p>
											</div>
										</div>
									))}

									<div className="flex gap-3 justify-end pt-4 border-t border-outline-variant/50">
										{order.status === "SHIPPED" ? (
											<button
												onClick={() => handleReceiveOrder(order.orderId)}
												disabled={processingOrderId === order.orderId}
												className="bg-primary hover:bg-primary/90 text-on-primary font-label-md py-2 px-6 rounded-lg transition-colors flex items-center justify-center gap-2 disabled:opacity-50 shadow-sm"
											>
												<span className="material-symbols-outlined text-[18px]">
													{processingOrderId === order.orderId ? "progress_activity" : "check_circle"}
												</span>
												{processingOrderId === order.orderId ? "Processing..." : "Confirm Receipt"}
											</button>
										) : (
											<button 
											    onClick={() => setSelectedOrder(order)}
											    className="text-on-surface-variant border border-outline-variant font-label-md py-2 px-6 rounded-lg hover:bg-surface-container-high transition-colors"
											>
												View Invoice
											</button>
										)}
									</div>
								</div>
							</article>
						);
					})
				) : (
					<div className="text-center py-16 bg-surface-container-lowest border border-outline-variant rounded-xl shadow-sm">
						<span className="material-symbols-outlined text-[48px] text-outline mb-3">
							order_play
						</span>
						<p className="text-on-surface-variant font-body-md">
							No orders match your filter criteria.
						</p>
					</div>
				)}
			</div>

			{showToast && (
				<div className="fixed bottom-6 right-6 z-50">
					<div className={`bg-inverse-surface text-inverse-on-surface border border-outline px-6 py-4 rounded-xl shadow-lg flex items-center gap-3 max-w-sm ${toastAnimationClass}`}>
						<span className="material-symbols-outlined text-primary-fixed">check_circle</span>
						<p className="font-body-md flex-1">{toastMessage}</p>
						<button onClick={handleCloseToast} className="text-inverse-on-surface hover:text-primary-fixed transition-colors">
							<span className="material-symbols-outlined text-[20px]">close</span>
						</button>
					</div>
				</div>
			)}
			
			{/* Order Detail Modal */}
			{selectedOrder && (
				<div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
					<div className="bg-surface w-full max-w-2xl rounded-2xl shadow-xl overflow-hidden flex flex-col max-h-[90vh]">
						<div className="flex items-center justify-between p-6 border-b border-outline-variant bg-surface-container-lowest">
							<div>
								<h2 className="font-headline-md text-on-surface">Invoice Details</h2>
								<p className="text-body-sm text-on-surface-variant">Order #{selectedOrder.orderId}</p>
							</div>
							<button 
								onClick={() => setSelectedOrder(null)}
								className="p-2 text-on-surface-variant hover:bg-surface-container rounded-full transition-colors"
							>
								<span className="material-symbols-outlined">close</span>
							</button>
						</div>
						
						<div className="p-6 overflow-y-auto flex-1 space-y-6">
							<div className="flex flex-wrap justify-between gap-4 p-4 bg-surface-container-low rounded-xl border border-outline-variant/50">
								<div>
									<p className="text-label-sm text-on-surface-variant uppercase tracking-wider">Date</p>
									<p className="font-body-md font-medium text-on-surface">{formatDate(selectedOrder.orderDate)}</p>
								</div>
								<div>
									<p className="text-label-sm text-on-surface-variant uppercase tracking-wider">Status</p>
									<p className="font-body-md font-medium text-on-surface">{getStatusDisplay(selectedOrder.status).label}</p>
								</div>
								<div>
									<p className="text-label-sm text-on-surface-variant uppercase tracking-wider">Total Amount</p>
									<p className="font-headline-md text-primary font-bold">Rp {selectedOrder.totalAmount.toLocaleString("id-ID")}</p>
								</div>
							</div>

							<div>
								<h3 className="font-label-md text-on-surface mb-3 uppercase tracking-wider">Order Items</h3>
								<div className="space-y-3">
									{selectedOrder.items.map((item, idx) => (
										<div key={idx} className="flex justify-between items-center p-4 border border-outline-variant rounded-xl bg-surface-container-lowest gap-4">
											<div className="flex items-center gap-4 flex-1">
												<div className="w-12 h-12 bg-surface-container-high rounded-md flex items-center justify-center shrink-0 overflow-hidden border border-outline-variant">
													{item.imageUrl ? (
														<img src={item.imageUrl} alt={item.productName} className="w-full h-full object-cover" />
													) : (
														<span className="material-symbols-outlined text-outline text-[20px]">inventory_2</span>
													)}
												</div>
												<div>
													<p className="font-label-md text-on-surface text-base">{item.productName}</p>
													<p className="text-body-sm text-on-surface-variant mt-1">
														{item.quantity} x Rp {item.pricePerItem.toLocaleString("id-ID")}
													</p>
												</div>
											</div>
											<p className="font-mono-data font-semibold text-on-surface">
												Rp {(item.quantity * item.pricePerItem).toLocaleString("id-ID")}
											</p>
										</div>
									))}
								</div>
							</div>
						</div>
						
						<div className="p-6 border-t border-outline-variant bg-surface-container-lowest flex justify-end gap-3">
							<button 
								onClick={() => setSelectedOrder(null)}
								className="px-6 py-2 bg-primary text-on-primary rounded-xl font-label-md hover:bg-primary-container transition-colors"
							>
								Close
							</button>
						</div>
					</div>
				</div>
			)}
		</div>
	);
};

export default OrderHistoryPage;
