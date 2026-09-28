import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { User, LoginPayload, SignupPayload } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly baseUrl = `${environment.apiUrl}/users`;
  private readonly USER_STORAGE_KEY = 'blog_post_current_user';

  currentUser = signal<User | null>(this.loadStoredUser());

  constructor(private http: HttpClient) {}

  private loadStoredUser(): User | null {
    const data = localStorage.getItem(this.USER_STORAGE_KEY);
    if (data) {
      try {
        return JSON.parse(data);
      } catch (e) {
        localStorage.removeItem(this.USER_STORAGE_KEY);
      }
    }
    return null;
  }

  login(payload: LoginPayload): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/login`, payload).pipe(
      tap((user: User) => this.setUser(user))
    );
  }

  signup(payload: SignupPayload): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/signup`, payload).pipe(
      tap((user: User) => this.setUser(user))
    );
  }

  logout(): void {
    localStorage.removeItem(this.USER_STORAGE_KEY);
    this.currentUser.set(null);
  }

  private setUser(user: User): void {
    localStorage.setItem(this.USER_STORAGE_KEY, JSON.stringify(user));
    this.currentUser.set(user);
  }

  getCurrentUser(): User | null {
    return this.currentUser();
  }
}
