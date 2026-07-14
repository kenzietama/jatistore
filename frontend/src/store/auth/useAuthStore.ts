import { create } from "zustand";
import { jwtDecode } from "jwt-decode";

interface UserDecoded {
	userId: string;
	sub: string; // Email
	role: string;
	exp: number;
}

interface AuthState {
	token: string | null;
	user: UserDecoded | null;
	isAuthenticated: boolean;
	setToken: (token: string | null) => void;
	logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => {
	// Initialize state from localStorage if available
	const initialToken = localStorage.getItem("jatistore_token");
	let initialUser: UserDecoded | null = null;
	let initialIsAuthenticated = false;

	if (initialToken) {
		try {
			const decoded = jwtDecode<UserDecoded>(initialToken);
			// Check if token is expired
			const currentTime = Date.now() / 1000;
			if (decoded.exp > currentTime) {
				initialUser = decoded;
				initialIsAuthenticated = true;
			} else {
				localStorage.removeItem("jatistore_token");
			}
		} catch (error) {
			localStorage.removeItem("jatistore_token");
		}
	}

	return {
		token: initialToken,
		user: initialUser,
		isAuthenticated: initialIsAuthenticated,
		setToken: (token: string | null) => {
			if (token) {
				localStorage.setItem("jatistore_token", token);
				try {
					const decoded = jwtDecode<UserDecoded>(token);
					set({
						token,
						user: decoded,
						isAuthenticated: true,
					});
				} catch (error) {
					console.error("Failed to decode JWT token:", error);
					localStorage.removeItem("jatistore_token");
					set({
						token: null,
						user: null,
						isAuthenticated: false,
					});
				}
			} else {
				localStorage.removeItem("jatistore_token");
				set({
					token: null,
					user: null,
					isAuthenticated: false,
				});
			}
		},
		logout: () => {
			localStorage.removeItem("jatistore_token");
			set({
				token: null,
				user: null,
				isAuthenticated: false,
			});
		},
	};
});
