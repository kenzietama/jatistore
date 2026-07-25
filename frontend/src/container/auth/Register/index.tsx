import React from "react";
import { useNavigate } from "react-router-dom";

const Register: React.FC = () => {
	const navigate = useNavigate();

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
						<p className="font-body-sm text-body-sm text-on-surface-variant">
							Join JatiStore as a buyer or seller today.
						</p>
					</div>

					{/* Registration form placeholder - will be added in Task 10 */}
					<div className="space-y-stack-md">
						<div className="bg-surface-container border border-outline-variant rounded-lg p-stack-md text-center">
							<span className="material-symbols-outlined text-on-surface-variant text-[48px] mb-2">
								edit_document
							</span>
							<p className="font-body-md text-body-md text-on-surface-variant">
								Registration form will be added in Task 10
							</p>
						</div>

						{/* Placeholder Submit Button */}
						<button
							className="w-full flex justify-center items-center py-2 px-4 border border-transparent rounded-lg shadow-sm font-label-md text-label-md text-on-primary bg-primary hover:bg-primary-container-variant focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary transition-all duration-200 active:scale-[0.98] h-[40px] gap-2 mt-stack-lg cursor-not-allowed opacity-50"
							disabled
							type="button"
						>
							<span>Create Account</span>
							<span className="material-symbols-outlined text-[18px]">
								arrow_forward
							</span>
						</button>
					</div>

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
