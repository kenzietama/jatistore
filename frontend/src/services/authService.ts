import axios from 'axios';

const API_BASE_URL = '/api/v1';

export interface RegisterRequest {
  email: string;
  username: string;
  fullName: string;
  phoneNumber: string;
  password: string;
  dateOfBirth?: string;
}

export interface RegisterResponse {
  code: string;
  status: string;
  message: string;
  data: null;
  timestamp: string;
  requestId: string;
}

export const register = async (data: RegisterRequest): Promise<RegisterResponse> => {
  const response = await axios.post<RegisterResponse>(
    `${API_BASE_URL}/auth/register`,
    data,
    {
      headers: {
        'X-Request-ID': crypto.randomUUID(),
        'Content-Type': 'application/json',
      },
    }
  );
  return response.data;
};
