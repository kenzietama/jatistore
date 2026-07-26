import React, { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { authService } from "../../../service/auth/authService";

interface FormData {
	email: string;
	username: string;
	fullName: string;
	phoneNumber: string;
	dateOfBirth: string;
	password: string;
	confirmPassword: string;
}

interface FormErrors {
	email?: string;
	username?: string;
	fullName?: string;
	phoneNumber?: string;
	password?: string;
	confirmPassword?: string;
}

const Register: React.FC = () => {
	const navigate = useNavigate();
	const [formData, setFormData] = useState<FormData>({
		email: "",
		username: "",
		fullName: "",
		phoneNumber: "",
		dateOfBirth: "",
		password: "",
		confirmPassword: "",
	});
	const [showPassword, setShowPassword] = useState(false);
	const [showConfirmPassword, setShowConfirmPassword] = useState(false);
	const [errors, setErrors] = useState<FormErrors>({});
	const [touched, setTouched] = useState<Record<string, boolean>>({});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [apiError, setApiError] = useState<string>("");
	const timeoutRef = useRef<number | null>(null);

	useEffect(() => {
		return () => {
			if (timeoutRef.current) {
				clearTimeout(timeoutRef.current);
			}
		};
	}, []);

	const validateField = (name: string, value: string): string | undefined => {
		switch (name) {
			case "email": {
				if (!value) return "Email is required";
				if (value.length > 255)
					return "Email must not exceed 255 characters";
				const emailRegex =
					/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
				if (!emailRegex.test(value)) return "Invalid email format";
				break;
			}
			case "username": {
				if (!value) return "Username is required";
				const usernameRegex = /^[a-zA-Z0-9_-]{4,20}$/;
				if (!usernameRegex.test(value))
					return "Username must be 4-20 characters, alphanumeric with underscore or dash only";
				break;
			}
			case "fullName": {
				if (!value) return "Full name is required";
				if (value.length < 2 || value.length > 100)
					return "Full name must be 2-100 characters";
				break;
			}
			case "phoneNumber": {
				if (!value) return "Phone number is required";
				const phoneRegex = /^\d{10,20}$/;
				if (!phoneRegex.test(value))
					return "Phone number must be 10-20 digits only";
				break;
			}
			case "password": {
				if (!value) return "Password is required";
				const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
				if (!passwordRegex.test(value))
					return "Password must be at least 8 characters with 1 uppercase, 1 lowercase, and 1 digit";
				break;
			}
			case "confirmPassword": {
				if (!value) return "Please confirm your password";
				if (value !== formData.password)
					return "Passwords do not match";
				break;
			}
		}
		return undefined;
	};

	const handleBlur = (e: React.FocusEvent<HTMLInputElement>) => {
		const { name, value } = e.target;
		setTouched((prev) => ({ ...prev, [name]: true }));
		const error = validateField(name, value);
		setErrors((prev) => ({ ...prev, [name]: error }));
	};

	const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const { name, value } = e.target;
		setFormData((prev) => ({
			...prev,
			[name]: value,
		}));
		// Revalidate if field has been touched
		if (touched[name]) {
			const error = validateField(name, value);
			setErrors((prev) => ({ ...prev, [name]: error }));
		}
		// Re-validate confirmPassword when password changes
		if (name === "password" && touched.confirmPassword) {
			if (
				formData.confirmPassword &&
				formData.confirmPassword !== value
			) {
				setErrors((prev) => ({
					...prev,
					confirmPassword: "Passwords do not match",
				}));
			} else {
				setErrors((prev) => ({ ...prev, confirmPassword: undefined }));
			}
		}
	};

	const isFormValid = (): boolean => {
		const requiredFields = [
			"email",
			"username",
			"fullName",
			"phoneNumber",
			"password",
			"confirmPassword",
		];
		const allFilled = requiredFields.every(
			(field) => formData[field as keyof FormData],
		);
		const noErrors = Object.keys(errors).every(
			(key) => !errors[key as keyof FormErrors],
		);
		return allFilled && noErrors;
	};

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		setApiError("");

		// Validate all fields on submit
		const newErrors: FormErrors = {};
		Object.keys(formData).forEach((key) => {
			const error = validateField(key, formData[key as keyof FormData]);
			if (error) newErrors[key as keyof FormErrors] = error;
		});

		setErrors(newErrors);
		setTouched({
			email: true,
			username: true,
			fullName: true,
			phoneNumber: true,
			password: true,
			confirmPassword: true,
		});

		if (Object.keys(newErrors).length > 0) return;

		setIsSubmitting(true);

		try {
			const registerData = {
				email: formData.email,
				username: formData.username,
				fullName: formData.fullName,
				phoneNumber: formData.phoneNumber,
				password: formData.password,
				dateOfBirth: formData.dateOfBirth || undefined,
			};

			const response = await authService.register(registerData);

			// Show success message
			alert(`${response.message} Redirecting to login...`);

			// Redirect to login after 2 seconds
			timeoutRef.current = window.setTimeout(() => {
				navigate("/auth/login");
			}, 2000);
		} catch (error: any) {
			if (error.response?.data) {
				const responseData = error.response.data;
				const httpStatus = error.response.status || responseData.code;

				if (
					responseData.error &&
					Object.keys(responseData.error).length > 0
				) {
					const fieldErrors = Object.entries(responseData.error)
						.map(([field, msg]) => `${field}: ${msg}`)
						.join(" | ");
					setApiError(fieldErrors);
				} else if (responseData.message) {
					setApiError(responseData.message);
				} else if (httpStatus === 409) {
					setApiError(
						"Email, username, or phone number is already registered.",
					);
				} else {
					setApiError("Registration failed. Please try again.");
				}
			} else {
				setApiError("Network error. Please try again later.");
			}
		} finally {
			setIsSubmitting(false);
		}
	};

	const togglePasswordVisibility = () => {
		setShowPassword(!showPassword);
	};

	const toggleConfirmPasswordVisibility = () => {
		setShowConfirmPassword(!showConfirmPassword);
	};

	return (
		<div className="bg-surface-container-low min-h-screen flex flex-col font-body-md text-on-surface">
			{/* Top Navigation Shell (Simplified for Register) */}
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
								person_add
							</span>
						</div>
						<h1 className="font-headline-lg text-headline-lg text-on-surface mb-unit">
							Create Account
						</h1>
					</div>

					{apiError && (
						<div className="mb-stack-md p-3 bg-error-container text-on-error-container rounded-lg border border-error flex items-start gap-2">
							<span className="material-symbols-outlined text-[20px] flex-shrink-0">
								error
							</span>
							<span className="font-body-sm text-body-sm">
								{apiError}
							</span>
						</div>
					)}

					<form className="space-y-stack-md" onSubmit={handleSubmit}>
						{/* Email Field */}
						<div className="space-y-unit">
							<label
								className="block font-label-md text-label-md text-on-surface"
								htmlFor="email"
							>
								Email Address *
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
									placeholder="Enter your email"
									type="email"
									required
									value={formData.email}
									onChange={handleChange}
									onBlur={handleBlur}
								/>
							</div>
							{touched.email && errors.email && (
								<p className="text-error text-label-sm font-label-sm mt-1 flex items-center gap-1">
									<span className="material-symbols-outlined text-[16px]">
										error
									</span>
									<span>{errors.email}</span>
								</p>
							)}
						</div>

						{/* Username Field */}
						<div className="space-y-unit">
							<label
								className="block font-label-md text-label-md text-on-surface"
								htmlFor="username"
							>
								Username *
							</label>
							<div className="relative">
								<div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
									<span className="material-symbols-outlined text-on-surface-variant text-[20px]">
										person
									</span>
								</div>
								<input
									className="block w-full pl-10 pr-3 py-2 border border-outline rounded-lg text-on-surface bg-surface focus:ring-2 focus:ring-primary focus:border-primary transition-shadow font-body-md text-body-md placeholder-on-surface-variant/50 h-[40px]"
									id="username"
									name="username"
									placeholder="Choose a username (4-20 characters)"
									type="text"
									required
									value={formData.username}
									onChange={handleChange}
									onBlur={handleBlur}
								/>
							</div>
							{touched.username && errors.username && (
								<p className="text-error text-label-sm font-label-sm mt-1 flex items-center gap-1">
									<span className="material-symbols-outlined text-[16px]">
										error
									</span>
									<span>{errors.username}</span>
								</p>
							)}
						</div>

						{/* Full Name Field */}
						<div className="space-y-unit">
							<label
								className="block font-label-md text-label-md text-on-surface"
								htmlFor="fullName"
							>
								Full Name *
							</label>
							<div className="relative">
								<div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
									<span className="material-symbols-outlined text-on-surface-variant text-[20px]">
										badge
									</span>
								</div>
								<input
									className="block w-full pl-10 pr-3 py-2 border border-outline rounded-lg text-on-surface bg-surface focus:ring-2 focus:ring-primary focus:border-primary transition-shadow font-body-md text-body-md placeholder-on-surface-variant/50 h-[40px]"
									id="fullName"
									name="fullName"
									placeholder="Enter your full name"
									type="text"
									required
									value={formData.fullName}
									onChange={handleChange}
									onBlur={handleBlur}
								/>
							</div>
							{touched.fullName && errors.fullName && (
								<p className="text-error text-label-sm font-label-sm mt-1 flex items-center gap-1">
									<span className="material-symbols-outlined text-[16px]">
										error
									</span>
									<span>{errors.fullName}</span>
								</p>
							)}
						</div>

						{/* Phone Number Field */}
						<div className="space-y-unit">
							<label
								className="block font-label-md text-label-md text-on-surface"
								htmlFor="phoneNumber"
							>
								Phone Number *
							</label>
							<div className="relative">
								<div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
									<span className="material-symbols-outlined text-on-surface-variant text-[20px]">
										phone
									</span>
								</div>
								<input
									className="block w-full pl-10 pr-3 py-2 border border-outline rounded-lg text-on-surface bg-surface focus:ring-2 focus:ring-primary focus:border-primary transition-shadow font-body-md text-body-md placeholder-on-surface-variant/50 h-[40px]"
									id="phoneNumber"
									name="phoneNumber"
									placeholder="Enter phone number (10-20 digits)"
									type="tel"
									required
									value={formData.phoneNumber}
									onChange={handleChange}
									onBlur={handleBlur}
								/>
							</div>
							{touched.phoneNumber && errors.phoneNumber && (
								<p className="text-error text-label-sm font-label-sm mt-1 flex items-center gap-1">
									<span className="material-symbols-outlined text-[16px]">
										error
									</span>
									<span>{errors.phoneNumber}</span>
								</p>
							)}
						</div>

						{/* Date of Birth Field */}
						<div className="space-y-unit">
							<label
								className="block font-label-md text-label-md text-on-surface"
								htmlFor="dateOfBirth"
							>
								Date of Birth
							</label>
							<div className="relative">
								<div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
									<span className="material-symbols-outlined text-on-surface-variant text-[20px]">
										calendar_today
									</span>
								</div>
								<input
									className="block w-full pl-10 pr-3 py-2 border border-outline rounded-lg text-on-surface bg-surface focus:ring-2 focus:ring-primary focus:border-primary transition-shadow font-body-md text-body-md placeholder-on-surface-variant/50 h-[40px]"
									id="dateOfBirth"
									name="dateOfBirth"
									type="date"
									value={formData.dateOfBirth}
									onChange={handleChange}
									onBlur={handleBlur}
								/>
							</div>
						</div>

						{/* Password Field */}
						<div className="space-y-unit">
							<label
								className="block font-label-md text-label-md text-on-surface"
								htmlFor="password"
							>
								Password *
							</label>
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
									placeholder="Create password (min 8 chars)"
									type={showPassword ? "text" : "password"}
									required
									value={formData.password}
									onChange={handleChange}
									onBlur={handleBlur}
								/>
								<button
									className="absolute inset-y-0 right-0 pr-3 flex items-center text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer"
									onClick={togglePasswordVisibility}
									type="button"
								>
									<span className="material-symbols-outlined text-[20px]">
										{showPassword
											? "visibility_off"
											: "visibility"}
									</span>
								</button>
							</div>
							{touched.password && errors.password && (
								<p className="text-error text-label-sm font-label-sm mt-1 flex items-center gap-1">
									<span className="material-symbols-outlined text-[16px]">
										error
									</span>
									<span>{errors.password}</span>
								</p>
							)}
						</div>

						{/* Confirm Password Field */}
						<div className="space-y-unit">
							<label
								className="block font-label-md text-label-md text-on-surface"
								htmlFor="confirmPassword"
							>
								Confirm Password *
							</label>
							<div className="relative">
								<div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
									<span className="material-symbols-outlined text-on-surface-variant text-[20px]">
										lock
									</span>
								</div>
								<input
									className="block w-full pl-10 pr-10 py-2 border border-outline rounded-lg text-on-surface bg-surface focus:ring-2 focus:ring-primary focus:border-primary transition-shadow font-body-md text-body-md h-[40px]"
									id="confirmPassword"
									name="confirmPassword"
									placeholder="Re-enter password"
									type={
										showConfirmPassword
											? "text"
											: "password"
									}
									required
									value={formData.confirmPassword}
									onChange={handleChange}
									onBlur={handleBlur}
								/>
								<button
									className="absolute inset-y-0 right-0 pr-3 flex items-center text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer"
									onClick={toggleConfirmPasswordVisibility}
									type="button"
								>
									<span className="material-symbols-outlined text-[20px]">
										{showConfirmPassword
											? "visibility_off"
											: "visibility"}
									</span>
								</button>
							</div>
							{touched.confirmPassword &&
								errors.confirmPassword && (
									<p className="text-error text-label-sm font-label-sm mt-1 flex items-center gap-1">
										<span className="material-symbols-outlined text-[16px]">
											error
										</span>
										<span>{errors.confirmPassword}</span>
									</p>
								)}
						</div>

						{/* Submit Button */}
						<button
							className="w-full flex justify-center items-center py-2 px-4 border border-transparent rounded-lg shadow-sm font-label-md text-label-md text-on-primary bg-primary hover:bg-primary-container-variant focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary transition-all duration-200 active:scale-[0.98] h-[40px] gap-2 mt-stack-lg cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
							type="submit"
							disabled={!isFormValid() || isSubmitting}
						>
							<span>
								{isSubmitting
									? "Registering..."
									: "Create Account"}
							</span>
							<span className="material-symbols-outlined text-[18px]">
								arrow_forward
							</span>
						</button>
					</form>

					{/* Link to Login */}
					<div className="mt-stack-lg text-center">
						<p className="font-body-sm text-body-sm text-on-surface-variant">
							Already have an account?{" "}
							<button
								onClick={() => navigate("/auth/login")}
								className="font-label-sm text-label-sm text-primary hover:text-primary-container-variant transition-colors underline"
								type="button"
							>
								Sign in here
							</button>
						</p>
					</div>
				</div>
			</main>
		</div>
	);
};

export default Register;
