import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { PostService } from '../../services/post.service';
import { AuthService } from '../../services/auth.service';
import { CreatePostPayload, UpdatePostPayload } from '../../models/post.model';

@Component({
  selector: 'app-post-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './post-form.component.html',
  styleUrl: './post-form.component.css'
})
export class PostFormComponent implements OnInit {
  isEditMode = false;
  postId?: number;

  title = '';
  body = '';
  tagsInput = '';
  firstComment = '';
  author = '';

  submitting = false;
  errorMessage = '';

  constructor(
    private postService: PostService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.author = user.username;
    }

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.postId = Number(idParam);
      this.loadPost(this.postId);
    }
  }

  loadPost(id: number): void {
    this.postService.getPostById(id).subscribe({
      next: (post) => {
        this.title = post.title;
        this.body = post.body;
        this.tagsInput = post.tags ? post.tags.join(', ') : '';
        if (post.author) {
          this.author = post.author;
        }
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Failed to load post for editing.';
      }
    });
  }

  onSubmit(form: any): void {
    if (form.invalid) {
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const tagsArray = this.tagsInput
      .split(',')
      .map((t) => t.trim())
      .filter((t) => t.length > 0);

    if (this.isEditMode && this.postId) {
      const payload: UpdatePostPayload = {
        title: this.title,
        body: this.body,
        tags: tagsArray
      };

      this.postService.updatePost(this.postId, payload).subscribe({
        next: () => {
          this.submitting = false;
          this.router.navigate(['/']);
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage = err?.error?.message || 'Failed to update post.';
        }
      });
    } else {
      const currentUser = this.authService.getCurrentUser();
      const payload: CreatePostPayload = {
        title: this.title,
        body: this.body,
        tags: tagsArray,
        author: this.author.trim() || (currentUser ? currentUser.username : 'anonymous'),
        firstComment: this.firstComment ? this.firstComment.trim() : undefined
      };

      this.postService.createPost(payload).subscribe({
        next: () => {
          this.submitting = false;
          this.router.navigate(['/']);
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage = err?.error?.message || 'Failed to create post.';
        }
      });
    }
  }
}
