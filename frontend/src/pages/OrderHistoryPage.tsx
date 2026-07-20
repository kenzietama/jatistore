import React, { useState, useEffect } from "react";
import api from "../lib/api";

interface OrderItem {
	productName: string;
	quantity: number;
	pricePerItem: number;
	isFlashSale: boolean;
}

interface Order {
	orderId: string;
	orderDate: string;
	totalAmount: number;
	status: "PENDING" | "PAID_ON_HOLD" | "SHIPPED" | "RECEIVED" | "CANCELLED";
	items: OrderItem[];
}

interface OrderHistoryPageProps {
	onNavigateHome: () => void;
	onNavigateCart: () => void;
	cartCount: number;
}

const OrderHistoryPage: React.FC<OrderHistoryPageProps> = ({
	onNavigateHome,
	onNavigateCart,
	cartCount,
}) => {
	const [orders, setOrders] = useState<Order[]>([]);
	const [isLoading, setIsLoading] = useState(true);
	const [filterPending, setFilterPending] = useState(true);
	const [filterPaidOnHold, setFilterPaidOnHold] = useState(true);
	const [filterShipped, setFilterShipped] = useState(true);
	const [filterReceived, setFilterReceived] = useState(true);
	const [filterCancelled, setFilterCancelled] = useState(true);
	const [searchQuery, setSearchQuery] = useState("");

	const [processingOrderId, setProcessingOrderId] = useState<string | null>(null);
	const [showToast, setShowToast] = useState(false);
	const [toastAnimationClass, setToastAnimationClass] = useState("toast-enter");
	const [toastMessage, setToastMessage] = useState("");

	useEffect(() => {
		fetchOrders();
	}, []);

	const fetchOrders = async () => {
		try {
			setIsLoading(true);
			const token = localStorage.getItem("jatistore_token");

			if (!token) {
				console.error("No auth token found");
				setIsLoading(false);
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
				console.error("No auth token found");
				setProcessingOrderId(null);
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
					class: "bg-tertiary-container/20 border-tertiary text-on-tertiary-container",
				};
			case "PAID_ON_HOLD":
				return {
					label: "Processing",
					icon: "schedule",
					class: "bg-tertiary-container/20 border-tertiary text-on-tertiary-container",
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
		const matchesStatus =
			(order.status === "PENDING" && filterPending) ||
			(order.status === "PAID_ON_HOLD" && filterPaidOnHold) ||
			(order.status === "SHIPPED" && filterShipped) ||
			(order.status === "RECEIVED" && filterReceived) ||
			(order.status === "CANCELLED" && filterCancelled);

		const matchesSearch =
			order.items.some((item) =>
				item.productName.toLowerCase().includes(searchQuery.toLowerCase()),
			) ||
			order.orderId.toLowerCase().includes(searchQuery.toLowerCase());

		return matchesStatus && matchesSearch;
	});

	return (
		<div className="bg-background text-on-background font-body-md min-h-screen flex flex-col relative overflow-x-hidden">
			<main className="flex-1 w-full max-w-container-max mx-auto px-margin-mobile md:px-margin-desktop py-stack-lg">
				<div className="mb-stack-lg">
					<h1 className="text-display-lg font-display-lg text-on-background mb-unit">
						Order History
					</h1>
					<p className="text-body-lg font-body-lg text-on-surface-variant">
						Track, manage, and review your recent purchases.
					</p>
				</div>

				<div className="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
					<aside className="lg:col-span-3">
						<div className="bg-surface-container-lowest border border-outline-variant rounded-xl p-stack-md sticky top-24">
							<h3 className="text-headline-md font-headline-md text-on-surface mb-stack-sm border-b border-outline-variant pb-2">
								Filters
							</h3>
							<div className="space-y-4">
								<div>
									<label className="text-label-sm font-label-sm text-on-surface-variant uppercase tracking-wider block mb-2">
										Status
									</label>
									<div className="flex flex-col gap-2">
										<label className="flex items-center gap-2 cursor-pointer group">
											<input
												type="checkbox"
												checked={filterPending}
												onChange={(e) => setFilterPending(e.target.checked)}
												className="rounded border-outline-variant text-primary focus:ring-primary w-4 h-4"
											/>
											<span className="text-body-sm font-body-sm text-on-surface group-hover:text-primary transition-colors">
												Pending
											</span>
										</label>
										<label className="flex items-center gap-2 cursor-pointer group">
											<input
												type="checkbox"
												checked={filterPaidOnHold}
												onChange={(e) => setFilterPaidOnHold(e.target.checked)}
												className="rounded border-outline-variant text-primary focus:ring-primary w-4 h-4"
											/>
											<span className="text-body-sm font-body-sm text-on-surface group-hover:text-primary transition-colors">
												Processing
											</span>
										</label>
										<label className="flex items-center gap-2 cursor-pointer group">
											<input
												type="checkbox"
												checked={filterShipped}
												onChange={(e) => setFilterShipped(e.target.checked)}
												className="rounded border-outline-variant text-primary focus:ring-primary w-4 h-4"
											/>
											<span className="text-body-sm font-body-sm text-on-surface group-hover:text-primary transition-colors">
												Shipped
											</span>
										</label>
										<label className="flex items-center gap-2 cursor-pointer group">
											<input
												type="checkbox"
												checked={filterReceived}
												onChange={(e) => setFilterReceived(e.target.checked)}
												className="rounded border-outline-variant text-primary focus:ring-primary w-4 h-4"
											/>
											<span className="text-body-sm font-body-sm text-on-surface group-hover:text-primary transition-colors">
												Received
											</span>
										</label>
										<label className="flex items-center gap-2 cursor-pointer group">
											<input
												type="checkbox"
												checked={filterCancelled}
												onChange={(e) => setFilterCancelled(e.target.checked)}
												className="rounded border-outline-variant text-primary focus:ring-primary w-4 h-4"
											/>
											<span className="text-body-sm font-body-sm text-on-surface group-hover:text-primary transition-colors">
												Cancelled
											</span>
										</label>
									</div>
								</div>

								<div>
									<label className="text-label-sm font-label-sm text-on-surface-variant uppercase tracking-wider block mb-2">
										Search
									</label>
									<input
										type="text"
										value={searchQuery}
										onChange={(e) => setSearchQuery(e.target.value)}
										placeholder="Product or order ID..."
										className="w-full border border-outline-variant rounded-lg h-10 px-3 text-body-sm font-body-sm bg-surface-container-lowest text-on-surface focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
									/>
								</div>
							</div>
						</div>
					</aside>

					<div className="lg:col-span-9">
						{isLoading ? (
							<div className="text-center py-12 font-body-md text-on-surface-variant">
								Loading orders...
							</div>
						) : filteredOrders.length > 0 ? (
							filteredOrders.map((order) => {
								const statusInfo = getStatusDisplay(order.status);
								return (
									<div
										key={order.orderId}
										className="bg-surface-container-lowest border border-outline-variant rounded-xl p-stack-md mb-stack-md shadow-sm"
									>
										<div className="flex flex-col sm:flex-row sm:items-center justify-between gap-stack-sm mb-stack-md pb-stack-sm border-b border-outline-variant">
											<div className="flex items-center gap-3">
												<span className="material-symbols-outlined text-primary">
													receipt_long
												</span>
												<div>
													<p className="text-label-md font-label-md text-on-surface font-mono-data">
														{order.orderId}
													</p>
													<p className="text-body-sm text-body-sm text-on-surface-variant">
														{formatDate(order.orderDate)}
													</p>
												</div>
											</div>
											<div
												className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-full border ${statusInfo.class}`}
											>
												<span className="material-symbols-outlined text-[16px]">
													{statusInfo.icon}
												</span>
												<span className="text-label-sm font-label-sm font-semibold">
													{statusInfo.label}
												</span>
											</div>
										</div>

										<div className="space-y-3 mb-stack-md">
											{order.items.map((item, idx) => (
												<div key={idx} className="flex justify-between items-start">
													<div className="flex-1">
														<p className="text-body-md font-body-md text-on-surface">
															{item.productName}
															{item.isFlashSale && (
																<span className="ml-2 text-label-sm text-error">
																	⚡ Flash Sale
																</span>
															)}
														</p>
														<p className="text-body-sm text-on-surface-variant">
															Qty: {item.quantity}
														</p>
													</div>
													<p className="text-body-md font-mono-data text-on-surface">
														Rp {(item.pricePerItem * item.quantity).toLocaleString("id-ID")}
													</p>
												</div>
											))}
										</div>

										<div className="flex justify-between items-center pt-stack-sm border-t border-outline-variant">
											<span className="text-label-md font-label-md text-on-surface-variant">
												Total
											</span>
											<span className="text-headline-md font-headline-md text-primary font-bold">
												Rp {order.totalAmount.toLocaleString("id-ID")}
											</span>
										</div>

										<div className="flex gap-2 mt-stack-md">
											{order.status === "SHIPPED" ? (
												<button
													onClick={() => handleReceiveOrder(order.orderId)}
													disabled={processingOrderId === order.orderId}
													className="flex-1 bg-primary hover:bg-primary/90 text-on-primary font-label-md text-label-md py-2.5 px-6 rounded-lg transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
												>
													<span className="material-symbols-outlined text-[18px]">
														{processingOrderId === order.orderId
															? "progress_activity"
															: "check_circle"}
													</span>
													{processingOrderId === order.orderId
														? "Processing..."
														: "Confirm Receipt"}
												</button>
											) : null}
										</div>
									</div>
								);
							})
						) : (
							<div className="text-center py-12 bg-surface-container-lowest border border-outline-variant rounded-xl">
								<span className="material-symbols-outlined text-[48px] text-outline mb-2">
									order_play
								</span>
								<p className="text-on-surface-variant">
									No orders match your filter criteria.
								</p>
							</div>
						)}
					</div>
				</div>
			</main>

			<footer className="w-full bg-surface border-t border-outline-variant py-stack-lg mt-auto">
				<div className="max-w-container-max mx-auto px-margin-desktop text-center">
					<p className="font-label-sm text-label-sm text-on-surface-variant">
						© 2026 JatiStore. All rights reserved.
					</p>
				</div>
			</footer>

			{showToast && (
				<div
					className="fixed bottom-6 right-6 z-50"
					id="toast-container"
				>
					<div
						className={`bg-inverse-surface text-inverse-on-surface border border-outline px-6 py-4 rounded-xl shadow-lg flex items-center gap-3 max-w-sm ${toastAnimationClass}`}
					>
						<span className="material-symbols-outlined text-primary-fixed">
							check_circle
						</span>
						<p className="font-body-md text-body-md flex-1">{toastMessage}</p>
						<button
							onClick={handleCloseToast}
							className="text-inverse-on-surface hover:text-primary-fixed transition-colors"
						>
							<span className="material-symbols-outlined text-[20px]">
								close
							</span>
						</button>
					</div>
				</div>
			)}
		</div>
	);
};

export default OrderHistoryPage;
