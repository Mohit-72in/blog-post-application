export interface User {
  id?: number;
  username: string;
  name: string;
}

export interface LoginPayload {
  username: string;
}

export interface SignupPayload {
  username: string;
  name: string;
}
