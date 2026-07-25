import api from "../../lib/api";

export interface RestApiResponse<T> {
	code: number;
	status: string;
	message: string;
	data: T;
	error: Record<string, string> | null;
	timestamp: string;
	requestId: string;
}

export interface AuthLoginResponse {
	accessToken: string;
	tokenType: string;
	expiresIn: number;
	role: string;
}

export interface AuthLoginRequest {
	email: string;
	password: string;
}

export interface AuthRegisterRequest {
	email: string;
	username: string;
	fullName: string;
	phoneNumber: string;
	password: string;
	dateOfBirth?: string;
}

export const authService = {
	login: async (
		payload: AuthLoginRequest,
	): Promise<RestApiResponse<AuthLoginResponse>> => {
		const response = await api.post<RestApiResponse<AuthLoginResponse>>(
			"/api/v1/auth/login",
			payload,
		);
		return response.data;
	},
	logout: async (): Promise<RestApiResponse<void>> => {
		const response = await api.post<RestApiResponse<void>>(
			"/api/v1/auth/logout",
		);
		return response.data;
	},
	register: async (
		payload: AuthRegisterRequest,
	): Promise<RestApiResponse<void>> => {
		const response = await api.post<RestApiResponse<void>>(
			"/api/v1/auth/register",
			payload,
		);
		return response.data;
	},
};
