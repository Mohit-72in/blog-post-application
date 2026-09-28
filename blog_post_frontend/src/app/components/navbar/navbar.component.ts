import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  showModal = false;
  isSignupMode = false;

  loginUsername = '';
  signupUsername = '';
  signupName = '';

  submitting = false;
  errorMessage = '';
  successMessage = '';

  constructor(public authService: AuthService, private router: Router) {}

  openModal(mode: 'login' | 'signup'): void {
    this.isSignupMode = mode === 'signup';
    this.errorMessage = '';
    this.successMessage = '';
    this.submitting = false;
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.errorMessage = '';
    this.successMessage = '';
    this.submitting = false;
  }

  toggleMode(): void {
    this.isSignupMode = !this.isSignupMode;
    this.errorMessage = '';
    this.successMessage = '';
    this.submitting = false;
  }

  onLogin(): void {
    if (!this.loginUsername.trim()) {
      this.errorMessage = 'Please enter a username.';
      return;
    }
    this.errorMessage = '';
    this.submitting = true;
    this.authService.login({ username: this.loginUsername.trim() }).subscribe({
      next: (user: any) => {
        this.submitting = false;
        this.successMessage = `Welcome back, ${user.name}!`;
        setTimeout(() => this.closeModal(), 600);
      },
      error: (err: any) => {
        this.submitting = false;
        this.errorMessage = err.error?.message || err.message || 'Login failed. Make sure the username exists or sign up.';
      }
    });
  }

  onSignup(): void {
    if (!this.signupUsername.trim() || !this.signupName.trim()) {
      this.errorMessage = 'Please fill out both username and name.';
      return;
    }
    this.errorMessage = '';
    this.submitting = true;
    this.authService.signup({
      username: this.signupUsername.trim(),
      name: this.signupName.trim()
    }).subscribe({
      next: (user: any) => {
        this.submitting = false;
        this.successMessage = `Account created successfully! Welcome, ${user.name}.`;
        setTimeout(() => this.closeModal(), 600);
      },
      error: (err: any) => {
        this.submitting = false;
        this.errorMessage = err.error?.message || err.message || 'Signup failed. Username may already be taken.';
      }
    });
  }

  quickLogin(username: string): void {
    this.loginUsername = username;
    this.onLogin();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
