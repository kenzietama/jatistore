import React, { useState, useEffect } from "react";
import api from "../lib/api";

interface Product {
	id: string;
	name: string;
	description: string;
	image: string;
	price: number;
	stock: number;
	remainingQuota?: number;
	discountTag?: string;
	originalPrice?: number;
	rating?: number;
	reviewsCount?: number;
	thumbnails?: string[];
	isFlashSale?: boolean;
	flashSaleEndTime?: string;
	store?: {
		id: string;
		storeName: string;
	};
}

interface ProductDetailPageProps {
	productId: string;
	onBackToCatalog: () => void;
	onAddToCart: (
		productId: string,
		quantity: number,
		priceToUse: number,
	) => void;
	onCartClick: () => void;
	isFromFlashSale?: boolean;
}

const ProductDetailPage: React.FC<ProductDetailPageProps> = ({
	productId,
	onBackToCatalog,
	onAddToCart,
	onCartClick,
	isFromFlashSale = false,
}) => {
	const [product, setProduct] = useState<Product | null>(null);
	const [quantity, setQuantity] = useState<number>(1);
	const [mainImage, setMainImage] = useState<string>("");
	const [isLoading, setIsLoading] = useState<boolean>(true);

	const [flashSaleEvent, setFlashSaleEvent] = useState<{
		name: string;
		endTime: string;
	} | null>(null);
	const [upcomingFlashSaleEvent, setUpcomingFlashSaleEvent] = useState<{
		name: string;
		startTime: string;
	} | null>(null);
	const [timeLeft, setTimeLeft] = useState({
		days: 0,
		hours: 0,
		minutes: 0,
		seconds: 0,
	});
	const [serverTimeOffset, setServerTimeOffset] = useState<number>(0);

	// 1. Fetch Product Details
	useEffect(() => {
		setIsLoading(true);
		api.get(`/api/v1/products/${productId}`)
			.then((response) => {
				const result = response.data;
				const fetchedProduct =
					result?.restApiResponseData || result?.data || result;

				if (fetchedProduct && fetchedProduct.id) {
					setProduct(fetchedProduct);
					setMainImage(
						fetchedProduct.image ||
							"https://placehold.co/600x400?text=No+Image",
					);
					setQuantity(1);
				} else {
					setProduct(null);
				}
				setIsLoading(false);
			})
			.catch((error) => {
				console.error("Failed to load product details:", error);
				setIsLoading(false);
			});
	}, [productId]);

	// 2. Fetch Active and Upcoming Flash Sale from Backend
	const checkFlashSaleStatus = async () => {
		try {
			const [activeRes, upcomingRes] = await Promise.all([
				api.get("/api/v1/public/flash-sale/active").catch(() => null),
				api.get("/api/v1/public/flash-sale/upcoming").catch(() => null),
			]);

			let hasActive = false;
			let timestampStr = null;

			if (
				activeRes?.data &&
				(activeRes.data.code === 200 ||
					activeRes.data.restApiResponseHttpCode === 200)
			) {
				timestampStr =
					activeRes.data.timestamp ||
					activeRes.data.restApiResponseTimestamp;
				const activeData =
					activeRes.data.data || activeRes.data.restApiResponseData;
				if (activeData && activeData.endTime) {
					setFlashSaleEvent({
						name: activeData.name || "Flash Sale",
						endTime: activeData.endTime,
					});
					setUpcomingFlashSaleEvent(null);
					hasActive = true;
				} else {
					setFlashSaleEvent(null);
				}
			} else {
				setFlashSaleEvent(null);
			}

			if (
				!hasActive &&
				upcomingRes?.data &&
				(upcomingRes.data.code === 200 ||
					upcomingRes.data.restApiResponseHttpCode === 200)
			) {
				if (!timestampStr)
					timestampStr =
						upcomingRes.data.timestamp ||
						upcomingRes.data.restApiResponseTimestamp;
				const upcomingData =
					upcomingRes.data.data ||
					upcomingRes.data.restApiResponseData;
				if (upcomingData && upcomingData.startTime) {
					setUpcomingFlashSaleEvent({
						name: upcomingData.name || "Upcoming Flash Sale",
						startTime: upcomingData.startTime,
					});
				} else {
					setUpcomingFlashSaleEvent(null);
				}
			} else {
				setUpcomingFlashSaleEvent(null);
			}

			if (timestampStr) {
				const serverTime = new Date(timestampStr).getTime();
				const localTime = new Date().getTime();
				setServerTimeOffset(serverTime - localTime);
			}
		} catch (error) {
			console.error("Failed to load flash sale status:", error);
		}
	};

	useEffect(() => {
		checkFlashSaleStatus();
	}, []);

	// 3. Real-time Countdown Timer based on Flash Sale
	useEffect(() => {
		let targetTime = 0;

		if (flashSaleEvent?.endTime) {
			targetTime = new Date(flashSaleEvent.endTime).getTime();
		} else if (upcomingFlashSaleEvent?.startTime) {
			targetTime = new Date(upcomingFlashSaleEvent.startTime).getTime();
		} else {
			return;
		}

		const timer = setInterval(() => {
			const now = new Date().getTime() + serverTimeOffset;
			const difference = targetTime - now;

			if (difference <= 0) {
				clearInterval(timer);
				setTimeLeft({ days: 0, hours: 0, minutes: 0, seconds: 0 });
				// Auto-refresh: Re-check flash sale status (will update state and trigger re-render)
				checkFlashSaleStatus();
			} else {
				const days = Math.floor(difference / (1000 * 60 * 60 * 24));
				const hours = Math.floor(
					(difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60),
				);
				const minutes = Math.floor(
					(difference % (1000 * 60 * 60)) / (1000 * 60),
				);
				const seconds = Math.floor((difference % (1000 * 60)) / 1000);

				setTimeLeft({ days, hours, minutes, seconds });
			}
		}, 1000);

		return () => clearInterval(timer);
	}, [flashSaleEvent, upcomingFlashSaleEvent, serverTimeOffset]);

	const handleQuantityChange = (type: "add" | "remove") => {
		if (!product) return;
		const maxQuantity = product.isFlashSale
			? (product.remainingQuota ?? product.stock)
			: product.stock;
		if (type === "add" && quantity < maxQuantity) {
			setQuantity((prev) => prev + 1);
		} else if (type === "remove" && quantity > 1) {
			setQuantity((prev) => prev - 1);
		}
	};

	if (isLoading) {
		return (
			<div className="min-h-screen flex items-center justify-center bg-surface">
				<p className="font-label-md text-on-surface-variant">
					Loading product details from database...
				</p>
			</div>
		);
	}

	if (!product) {
		return (
			<div className="min-h-screen flex flex-col items-center justify-center bg-surface gap-4">
				<p className="font-label-md text-error">
					Product not found or has been deleted.
				</p>
				<button
					onClick={onBackToCatalog}
					className="text-primary font-semibold hover:underline"
				>
					Back to Catalog
				</button>
			</div>
		);
	}

	const finalProductRating = product.rating ?? 5;
	const finalReviewsCount = product.reviewsCount ?? 24;
	const finalSoldBy = product.store?.storeName ?? "JatiStore Official";
	const finalThumbnails = product.thumbnails ?? [];

	// ⚡ Consistent Active Flash Sale Status
	const isFlashSaleActive = Boolean(
		flashSaleEvent &&
		(product.isFlashSale || isFromFlashSale || product.discountTag),
	);

	// Consistent Original & Discount Price Calculation
	const isProductPricedAsDiscounted =
		isFlashSaleActive &&
		product.originalPrice &&
		product.originalPrice > product.price;

	const displayOriginalPrice = isProductPricedAsDiscounted
		? product.originalPrice
		: isFlashSaleActive
			? Math.round(product.price * 1.3)
			: null;

	const displayCurrentPrice = isProductPricedAsDiscounted
		? product.price
		: isFlashSaleActive
			? product.price
			: product.originalPrice || product.price;

	return (
		<>
			<main className="flex-grow w-full max-w-container-max mx-auto px-margin-desktop py-stack-lg flex flex-col gap-stack-lg">
				{/* Breadcrumb / Back Button */}
				<button
					onClick={onBackToCatalog}
					className="flex items-center gap-1 text-primary text-body-sm font-semibold hover:underline self-start"
				>
					<span className="material-symbols-outlined text-[18px]">
						arrow_back
					</span>{" "}
					Back to Catalog
				</button>

				{/* Product Section */}
				<div className="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
					{/* Image Gallery */}
					<div className="lg:col-span-7 flex flex-col gap-stack-sm">
						<div className="w-full flex items-center justify-center aspect-[4/3] rounded-xl overflow-hidden bg-surface-container-lowest border border-outline-variant shadow-sm relative group">
							<img
								alt={product.name}
								className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
								src={mainImage}
							/>

							{isFlashSaleActive && (
								<div className="absolute top-4 left-4 bg-error text-on-error px-3.5 py-1.5 rounded-full font-label-sm text-label-sm uppercase tracking-wide shadow-md flex items-center gap-1.5 z-10">
									<span className="material-symbols-outlined text-[18px]">
										bolt
									</span>
									{product.discountTag || "Flash Sale"}
								</div>
							)}
						</div>

						{/* Thumbnails */}
						<div className="flex gap-stack-sm overflow-x-auto pb-2 snap-x">
							<div
								onClick={() =>
									setMainImage(
										product.image ||
											"https://placehold.co/600x400?text=No+Image",
									)
								}
								className={`w-24 h-24 shrink-0 rounded-lg border-2 overflow-hidden cursor-pointer snap-start transition-all ${mainImage === product.image ? "border-primary opacity-100" : "border-outline-variant opacity-70 hover:opacity-100"}`}
							>
								<img
									alt="Main image thumbnail"
									className="w-full h-full object-cover"
									src={
										product.image ||
										"https://placehold.co/600x400?text=No+Image"
									}
								/>
							</div>
							{finalThumbnails.map((thumb, index) => (
								<div
									key={index}
									onClick={() => setMainImage(thumb)}
									className={`w-24 h-24 shrink-0 rounded-lg border-2 overflow-hidden cursor-pointer snap-start transition-all ${mainImage === thumb ? "border-primary opacity-100" : "border-outline-variant opacity-70 hover:opacity-100"}`}
								>
									<img
										alt={`Thumbnail ${index + 1}`}
										className="w-full h-full object-cover"
										src={thumb}
									/>
								</div>
							))}
						</div>
					</div>

					{/* Product Details */}
					<div className="lg:col-span-5 flex flex-col gap-stack-md bg-surface-container-lowest p-gutter rounded-xl shadow-sm border border-outline-variant">
						{isFlashSaleActive && (
							<div className="bg-gradient-to-r from-error/15 to-error/5 border border-error/30 rounded-xl p-3.5 flex items-center justify-between shadow-sm mb-2">
								<div className="flex items-center gap-2 text-error">
									<span className="material-symbols-outlined text-[22px]">
										bolt
									</span>
									<span className="font-label-md font-bold uppercase tracking-wider text-xs">
										Flash Sale Ends In:
									</span>
								</div>
								<div className="flex items-center gap-1 font-mono font-bold text-error text-sm">
									<span className="bg-error/20 px-2 py-0.5 rounded">
										{String(timeLeft.days).padStart(2, "0")}
									</span>
									:
									<span className="bg-error/20 px-2 py-0.5 rounded">
										{String(timeLeft.hours).padStart(
											2,
											"0",
										)}
									</span>
									:
									<span className="bg-error/20 px-2 py-0.5 rounded">
										{String(timeLeft.minutes).padStart(
											2,
											"0",
										)}
									</span>
									:
									<span className="bg-error/20 px-2 py-0.5 rounded">
										{String(timeLeft.seconds).padStart(
											2,
											"0",
										)}
									</span>
								</div>
							</div>
						)}

						{(() => {
							const isWithinOneHour =
								timeLeft.days === 0 &&
								(timeLeft.hours === 0 ||
									(timeLeft.hours === 1 &&
										timeLeft.minutes === 0 &&
										timeLeft.seconds === 0));
							return (
								!flashSaleEvent &&
								upcomingFlashSaleEvent &&
								isWithinOneHour && (
									<div className="bg-gradient-to-r from-secondary-container/30 to-secondary-container/10 border border-secondary/30 rounded-xl p-3.5 flex items-center justify-between shadow-sm mb-2">
										<div className="flex items-center gap-2 text-secondary">
											<span className="material-symbols-outlined text-[22px]">
												schedule
											</span>
											<span className="font-label-md font-bold uppercase tracking-wider text-xs">
												Flash Sale Starts In:
											</span>
										</div>
										<div className="flex items-center gap-1 font-mono font-bold text-secondary text-sm">
											<span className="bg-secondary-container/50 px-2 py-0.5 rounded">
												{String(timeLeft.days).padStart(
													2,
													"0",
												)}
											</span>
											:
											<span className="bg-secondary-container/50 px-2 py-0.5 rounded">
												{String(
													timeLeft.hours,
												).padStart(2, "0")}
											</span>
											:
											<span className="bg-secondary-container/50 px-2 py-0.5 rounded">
												{String(
													timeLeft.minutes,
												).padStart(2, "0")}
											</span>
											:
											<span className="bg-secondary-container/50 px-2 py-0.5 rounded">
												{String(
													timeLeft.seconds,
												).padStart(2, "0")}
											</span>
										</div>
									</div>
								)
							);
						})()}

						<div>
							<div className="flex items-center gap-1 mb-1 text-on-surface-variant">
								<span
									className="material-symbols-outlined text-label-sm"
									style={{ fontSize: "16px" }}
								>
									storefront
								</span>
								<span className="font-label-sm text-label-sm uppercase tracking-wider">
									Sold by {finalSoldBy}
								</span>
							</div>
							<h1 className="text-headline-lg font-headline-lg text-on-surface mb-2">
								{product.name}
							</h1>
							<div className="flex items-center gap-2 mb-4">
								<div className="flex text-tertiary-fixed-dim">
									{Array.from({
										length: Math.floor(finalProductRating),
									}).map((_, i) => (
										<span
											key={i}
											className="material-symbols-outlined text-[20px]"
											style={{
												fontVariationSettings:
													"'FILL' 1",
											}}
										>
											star
										</span>
									))}
								</div>
								<span className="font-body-sm text-body-sm text-on-surface-variant underline cursor-pointer hover:text-primary transition-colors">
									({finalReviewsCount} Reviews)
								</span>
							</div>
						</div>

						{/* PRODUCT PRICE: Synchronized between UI and cart */}
						<div className="flex flex-col pb-stack-md border-b border-outline-variant">
							<div className="flex items-baseline gap-3">
								<span
									className={`text-display-lg font-display-lg ${isFlashSaleActive ? "text-error font-bold" : "text-on-surface font-bold"}`}
								>
									Rp{" "}
									{displayCurrentPrice.toLocaleString(
										"id-ID",
									)}
								</span>
							</div>
							{displayOriginalPrice && isFlashSaleActive && (
								<div className="flex items-center gap-2 mt-1">
									<span className="text-headline-sm font-headline-sm text-on-surface-variant line-through opacity-70">
										Rp{" "}
										{displayOriginalPrice.toLocaleString(
											"id-ID",
										)}
									</span>
									<span className="text-xs bg-error/10 text-error font-bold px-2 py-0.5 rounded">
										Hemat Rp{" "}
										{(
											displayOriginalPrice -
											displayCurrentPrice
										).toLocaleString("id-ID")}
									</span>
								</div>
							)}
						</div>

						<p className="font-body-md text-body-md text-on-surface-variant leading-relaxed">
							{product.description ||
								"No description available for this product."}
						</p>

						<div className="flex items-center gap-4 py-2">
							<span className="flex items-center justify-center w-auto px-3 h-8 rounded bg-surface-container-high text-on-surface font-mono-data text-mono-data">
								Stok:{" "}
								{product.isFlashSale
									? (product.remainingQuota ?? product.stock)
									: product.stock}
							</span>
							<span className="font-label-sm text-label-sm text-on-surface-variant uppercase tracking-wider">
								In Stock
							</span>
						</div>

						<div className="mt-auto pt-stack-md flex gap-stack-sm">
							<div className="flex border border-outline-variant rounded-lg overflow-hidden h-12 w-32 bg-surface">
								<button
									onClick={() =>
										handleQuantityChange("remove")
									}
									className="w-15 flex items-center justify-center text-on-surface-variant hover:bg-surface-container transition-colors"
								>
									<span className="material-symbols-outlined">
										remove
									</span>
								</button>
								<input
									className="w-full text-center border-none bg-transparent font-body-md text-body-md focus:ring-0 p-0"
									type="text"
									readOnly
									value={quantity}
								/>
								<button
									onClick={() => handleQuantityChange("add")}
									className="w-15 flex items-center justify-center text-on-surface-variant hover:bg-surface-container transition-colors"
								>
									<span className="material-symbols-outlined">
										add
									</span>
								</button>
							</div>

							<button
								onClick={async () => {
									// Ensure cart price is synchronized with the currently displayed active price
									await onAddToCart(
										product.id,
										quantity,
										displayCurrentPrice,
									);
									onCartClick();
								}}
								className={`flex-1 font-label-md text-label-md py-3 px-6 rounded-full transition-colors flex items-center justify-center gap-2 shadow-sm ${
									isFlashSaleActive
										? "bg-error text-on-error hover:bg-error/90"
										: "bg-primary text-on-primary hover:bg-primary/90"
								}`}
							>
								<span className="material-symbols-outlined">
									shopping_cart
								</span>
								Add to Cart
							</button>
						</div>
					</div>
				</div>

				{/* Secondary Section */}
				<div className="grid grid-cols-1 lg:grid-cols-3 gap-gutter mt-stack-lg">
					<div className="lg:col-span-1 bg-surface-container-low rounded-xl p-gutter border border-outline-variant">
						<h3 className="text-headline-md font-headline-md text-on-surface mb-stack-md flex items-center gap-2">
							<span className="material-symbols-outlined text-[20px] text-primary">
								forum
							</span>{" "}
							Reviews
						</h3>
						<div className="flex items-center gap-4 mb-6">
							<div className="text-display-lg font-display-lg text-on-surface">
								{finalProductRating.toFixed(1)}
							</div>
							<div className="flex flex-col">
								<div className="flex text-tertiary-fixed-dim">
									{Array.from({
										length: Math.floor(finalProductRating),
									}).map((_, i) => (
										<span
											key={i}
											className="material-symbols-outlined"
											style={{
												fontVariationSettings:
													"'FILL' 1",
											}}
										>
											star
										</span>
									))}
								</div>
								<span className="font-label-sm text-label-sm text-on-surface-variant">
									Based on {finalReviewsCount} reviews
								</span>
							</div>
						</div>
						<button className="w-full py-2 border border-primary text-primary hover:bg-primary hover:text-on-primary rounded-lg font-label-md text-label-md transition-colors">
							Write a Review
						</button>
					</div>

					<div className="lg:col-span-2 bg-surface-container-lowest rounded-xl p-gutter border border-outline-variant shadow-sm">
						<h3 className="text-headline-md font-headline-md text-on-surface mb-stack-md flex items-center gap-2">
							<span className="material-symbols-outlined text-[20px] text-primary">
								interests
							</span>{" "}
							Related Products
						</h3>
						<div className="grid grid-cols-2 md:grid-cols-3 gap-stack-md">
							<div className="group cursor-pointer">
								<div className="aspect-square rounded-lg overflow-hidden bg-surface-container mb-2 relative">
									<img
										alt="Steel Bottle"
										className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
										src="https://lh3.googleusercontent.com/aida-public/AB6AXuDABg9qwjJl_K4ehHWRvKqVCspG_paSEXXqJJOMzgM7tj_72VisNit0zdRVayrWukirhTQqGOwqFDyEpE7jYVaD3_m1lenI6Rzd2HIgJnrfsU49cdAthjMgNm1FhOuBKdmHkQANxvXLo_m3ZgD9ioZNq1Fxz6tAkqQrrRF2d-GAbiNRZFxPoch5rFILfb9gICbbM1XcpNgYUPwnq83cVyMklFqzJTfKPvkdExNoeqNCmrlBv6W4LuVcy5HGpYbLWJHDnaw2AZk7qxrS"
									/>
								</div>
								<p className="text-label-sm font-label-sm text-on-surface-variant/70 mb-1">
									Eco Gear
								</p>
								<h4 className="font-label-md text-label-md text-on-surface truncate group-hover:text-primary transition-colors">
									Reusable Steel Bottle
								</h4>
								<p className="font-body-sm text-body-sm text-on-surface-variant">
									Rp 350.000
								</p>
							</div>
							<div className="group cursor-pointer">
								<div className="aspect-square rounded-lg overflow-hidden bg-surface-container mb-2 relative">
									<img
										alt="Cotton Bags"
										className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
										src="https://lh3.googleusercontent.com/aida-public/AB6AXuATr8f6Ui4Nq-HHwI1JP-Dn_6BpJRQFOHM9c1xMHHB_meQ3__PhqXH07u6WZFHD3aPBDuuZ6KlesCC0F7L5YExq0rzxtWOUrJPyg6dT9nXx-3wJ2gXo_o6sxyRySsLxnRAcdOpVcviGeAUVtzHVhBXQeEMdixF-B0nqOcoM5gnjFDywvT0EDmiVSXAQEkQ7qqbhUpbRYPNTw978r1F9CVp6lb9TJ8byW5QbyrqtLVa_PuylT6_MY6yxUfIqHPyVra3UkHu7LIGe7eJk"
									/>
								</div>
								<p className="text-label-sm font-label-sm text-on-surface-variant/70 mb-1">
									Green Living
								</p>
								<h4 className="font-label-md text-label-md text-on-surface truncate group-hover:text-primary transition-colors">
									Organic Produce Bags
								</h4>
								<p className="font-body-sm text-body-sm text-on-surface-variant">
									Rp 120.000
								</p>
							</div>
						</div>
					</div>
				</div>
			</main>
		</>
	);
};

export default ProductDetailPage;
