import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { authService } from "../../../service/auth/authService";
import { useAuthStore } from "../../../store/auth/useAuthStore";

const Login: React.FC = () => {
	const [email, setEmail] = useState("");
	const [password, setPassword] = useState("");
	const [showPassword, setShowPassword] = useState(false);
	const [validationError, setValidationError] = useState<string | null>(null);
	const [authError, setAuthError] = useState<string | null>(null);
	const [isLoading, setIsLoading] = useState(false);
	const navigate = useNavigate();
	const location = useLocation();
	const hasJustLoggedIn = React.useRef(false);

	const setToken = useAuthStore((state) => state.setToken);
	const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

	useEffect(() => {
		if (isAuthenticated && !hasJustLoggedIn.current) {
			navigate("/", { replace: true });
		}
	}, [isAuthenticated, navigate]);

	const togglePasswordVisibility = () => {
		setShowPassword(!showPassword);
	};

	const handleLogin = async (e: React.SubmitEvent<HTMLFormElement>) => {
		e.preventDefault();

		// Reset errors
		setValidationError(null);
		setAuthError(null);

		// Simple client side validation
		if (!email || !password) {
			setValidationError("Email and Password are required!");
			return;
		}

		setIsLoading(true);

		try {
			const response = await authService.login({ email, password });

			if (response.data?.accessToken) {
				hasJustLoggedIn.current = true;
				setToken(response.data.accessToken);
				// Success path: Redirect or load role-specific views
				const role = response.data.role;

				if (role === "SELLER") {
					navigate("/seller/dashboard");
				} else if (role === "BUYER") {
					navigate("/", { state: location.state || {} });
				} else if (role === "ADMIN") {
					navigate("/admin/dashboard");
				} else {
					navigate("/");
				}
			} else {
				console.error("Access token missing in response:", response);
				setAuthError("Failed to retrieve access token from response.");
			}
		} catch (error: any) {
			// Extract structured backend validation error or generic auth error
			if (error.response?.data) {
				const responseData = error.response.data;
				// Check for validation field errors (e.g. from RestControllerAdviceHandler)
				if (
					responseData.error &&
					Object.keys(responseData.error).length > 0
				) {
					const fieldErrors = Object.entries(responseData.error)
						.map(([field, msg]) => `${field}: ${msg}`)
						.join(" | ");
					setValidationError(
						fieldErrors || "Validation error occurred.",
					);
				} else {
					setAuthError(
						responseData.message || "Authentication failed.",
					);
				}
			} else {
				console.error("Network error:", error);
				setAuthError("Network error. Please try again later.");
			}
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<div className="bg-surface-container-low min-h-screen flex flex-col font-body-md text-on-surface">
			{/* Top Navigation Shell (Simplified for Login) */}
			<header className="bg-surface border-b border-outline-variant shadow-sm w-full top-0 z-50">
				<div className="flex justify-between items-center w-full px-margin-desktop max-w-container-max mx-auto h-16">
					{/* Brand Logo */}
					<a className="flex items-center gap-2 group" href="/">
						<span className="material-symbols-outlined text-primary text-[32px] group-hover:scale-110 transition-transform">
							storefront
						</span>
						<span className="text-headline-md font-headline-lg font-bold text-primary">
							JatiStore
						</span>
					</a>
				</div>
			</header>

			{/* Main Content Area */}
			<main className="flex-grow flex items-center justify-center p-margin-mobile md:p-margin-desktop">
				<div className="w-full max-w-md bg-surface-container-lowest rounded-xl shadow-lg border border-outline-variant p-stack-lg relative overflow-hidden">
					{/* Decorative accent */}
					<div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-primary to-secondary"></div>

					<div className="text-center mb-stack-lg">
						<div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary-container text-on-primary-container mb-stack-sm shadow-sm">
							<span className="material-symbols-outlined text-[32px]">
								login
							</span>
						</div>
						<h1 className="font-headline-lg text-headline-lg text-on-surface mb-unit">
							Welcome Back
						</h1>
						<p className="font-body-sm text-body-sm text-on-surface-variant">
							Buyers, Sellers, and Admins all sign in here.
						</p>
					</div>

					{/* Error Alerts */}
					{validationError && (
						<div className="bg-error-container text-on-error-container p-stack-sm rounded-lg mb-stack-md flex items-start gap-3 text-label-sm font-label-sm border border-error/20 animate-shake">
							<span className="material-symbols-outlined text-[20px] mt-0.5">
								error
							</span>
							<span>{validationError}</span>
						</div>
					)}

					{authError && (
						<div className="bg-error-container text-on-error-container p-stack-sm rounded-lg mb-stack-md flex items-start gap-3 text-label-sm font-label-sm border border-error/20 animate-shake">
							<span className="material-symbols-outlined text-[20px] mt-0.5">
								warning
							</span>
							<span>{authError}</span>
						</div>
					)}

					<form className="space-y-stack-md" onSubmit={handleLogin}>
						{/* Email Field */}
						<div className="space-y-unit">
							<label
								className="block font-label-md text-label-md text-on-surface"
								htmlFor="email"
							>
								Email Address
							</label>
							<div className="relative">
								<div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
									<span className="material-symbols-outlined text-on-surface-variant text-[20px]">
										mail
									</span>
								</div>
								<input
									className="block w-full pl-10 pr-3 py-2 border border-outline rounded-lg text-on-surface bg-surface focus:ring-2 focus:ring-primary focus:border-primary transition-shadow font-body-md text-body-md placeholder-on-surface-variant/50 h-[40px]"
									id="email"
									name="email"
									placeholder="name@example.com"
									type="email"
									disabled={isLoading}
									value={email}
									onChange={(e) => setEmail(e.target.value)}
								/>
							</div>
						</div>

						{/* Password Field */}
						<div className="space-y-unit">
							<div className="flex justify-between items-center">
								<label
									className="block font-label-md text-label-md text-on-surface"
									htmlFor="password"
								>
									Password
								</label>
							</div>
							<div className="relative">
								<div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
									<span className="material-symbols-outlined text-on-surface-variant text-[20px]">
										lock
									</span>
								</div>
								<input
									className="block w-full pl-10 pr-10 py-2 border border-outline rounded-lg text-on-surface bg-surface focus:ring-2 focus:ring-primary focus:border-primary transition-shadow font-body-md text-body-md h-[40px]"
									id="password"
									name="password"
									placeholder="••••••••"
									type={showPassword ? "text" : "password"}
									disabled={isLoading}
									value={password}
									onChange={(e) =>
										setPassword(e.target.value)
									}
								/>
								<button
									className="absolute inset-y-0 right-0 pr-3 flex items-center text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer"
									onClick={togglePasswordVisibility}
									disabled={isLoading}
									type="button"
								>
									<span
										className="material-symbols-outlined text-[20px]"
										id="password-toggle-icon"
									>
										{showPassword
											? "visibility_off"
											: "visibility"}
									</span>
								</button>
							</div>
						</div>

						{/* Action Button */}
						<button
							className="w-full flex justify-center items-center py-2 px-4 border border-transparent rounded-lg shadow-sm font-label-md text-label-md text-on-primary bg-primary hover:bg-primary-container-variant focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary transition-all duration-200 active:scale-[0.98] h-[40px] gap-2 mt-stack-lg cursor-pointer"
							disabled={isLoading}
							type="submit"
						>
							<span>
								{isLoading ? "Signing In..." : "Sign In"}
							</span>
							<span className="material-symbols-outlined text-[18px]">
								arrow_forward
							</span>
						</button>
					</form>

					<div className="mt-stack-lg pt-stack-md border-t border-outline-variant text-center">
						<p className="font-body-sm text-body-sm text-on-surface-variant">
							Don't have an account?{" "}
							<a
								className="font-label-md text-label-md text-primary hover:underline underline-offset-4 decoration-2 decoration-primary/30 hover:decoration-primary transition-all"
								onClick={() => navigate("/auth/register")}
							>
								Sign up
							</a>
						</p>
					</div>
				</div>
			</main>
		</div>
	);
};

export default Login;
