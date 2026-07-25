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
  // Format 1
  code?: string;
  status?: string;
  message?: string;
  data?: null;
  timestamp?: string;
  requestId?: string;
  // Format 2
  restApiResponseHttpCode?: number;
  restApiResponseHttpStatus?: string;
  restApiResponseMessage?: string;
  restApiResponseData?: null;
  restApiResponseTimestamp?: string;
  restApiResponseRequestId?: string;
}

export const register = async (data: RegisterRequest): Promise<{ message: string }> => {
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

  // Normalize to consistent format
  const message = response.data.message || response.data.restApiResponseMessage || 'Registration successful';
  return { message };
};
