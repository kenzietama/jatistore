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

export const authService = {
	login: async (payload: AuthLoginRequest): Promise<RestApiResponse<AuthLoginResponse>> => {
		const response = await api.post<RestApiResponse<AuthLoginResponse>>("/api/v1/auth/login", payload);
		return response.data;
	},
};
