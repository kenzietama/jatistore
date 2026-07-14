import axios from "axios";
import { useAuthStore } from "../store/auth/useAuthStore";

const baseURL = import.meta.env.VITE_BASE_URL || "http://localhost:8080";

const api = axios.create({
	baseURL,
	headers: {
		"Content-Type": "application/json",
	},
});

// Request interceptor to automatically attach authorization header
api.interceptors.request.use(
	(config) => {
		const token = useAuthStore.getState().token;
		if (token) {
			config.headers.Authorization = `Bearer ${token}`;
		}
		return config;
	},
	(error) => {
		return Promise.reject(error);
	}
);

// Response interceptor to handle authorization errors globally
api.interceptors.response.use(
	(response) => response,
	(error) => {
		if (error.response && error.response.status === 401) {
			// Clear credentials if token expires or is rejected by backend
			useAuthStore.getState().logout();
		}
		return Promise.reject(error);
	}
);

export default api;
