import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuthStore } from "../../store/auth/useAuthStore";

interface ProtectedRouteProps {
	allowedRoles?: string[];
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ allowedRoles }) => {
	const { isAuthenticated, user } = useAuthStore();

	if (!isAuthenticated) {
		// Not logged in, redirect to login page
		return <Navigate to="/auth/login" replace />;
	}

	if (allowedRoles && user && !allowedRoles.includes(user.role)) {
		// Logged in but does not have the required role
		// Redirect based on their role
		if (user.role === "SELLER") return <Navigate to="/seller/dashboard" replace />;
		if (user.role === "BUYER") return <Navigate to="/" replace />;
		if (user.role === "ADMIN") return <Navigate to="/admin/dashboard" replace />;
		return <Navigate to="/auth/login" replace />; // Fallback
	}

	// Authorized, render children routes
	return <Outlet />;
};

export default ProtectedRoute;
