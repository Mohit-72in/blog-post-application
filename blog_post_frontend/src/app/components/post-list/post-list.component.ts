import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { PostService } from '../../services/post.service';
import { AuthService } from '../../services/auth.service';
import { Post } from '../../models/post.model';
import { PostCardComponent } from '../post-card/post-card.component';

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [CommonModule, RouterModule, PostCardComponent],
  templateUrl: './post-list.component.html',
  styleUrl: './post-list.component.css'
})
export class PostListComponent implements OnInit {
  posts: Post[] = [];
  loading = true;
  errorMessage = '';
  filterAuthor: string | null = null;

  constructor(
    private postService: PostService,
    public authService: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params: any) => {
      this.filterAuthor = params['author'] || null;
      this.fetchPosts();
    });
  }

  fetchPosts(): void {
    this.loading = true;
    this.errorMessage = '';
    this.postService.getPosts(this.filterAuthor || undefined).subscribe({
      next: (data: Post[]) => {
        this.posts = data;
        this.loading = false;
      },
      error: (err: any) => {
        this.errorMessage = err?.error?.message || 'Failed to load posts from server.';
        this.loading = false;
      }
    });
  }

  onDeletePost(id: number): void {
    this.postService.deletePost(id).subscribe({
      next: () => {
        this.posts = this.posts.filter((p: Post) => p.id !== id);
      },
      error: (err: any) => {
        this.errorMessage = err?.error?.message || 'Failed to delete post.';
      }
    });
  }

  onReactPost(id: number): void {
    this.postService.reactToPost(id).subscribe({
      next: (updatedPost: Post) => {
        const post = this.posts.find((p: Post) => p.id === id);
        if (post) {
          post.reactions = updatedPost.reactions;
        }
      },
      error: (err: any) => {
        this.errorMessage = err?.error?.message || 'Failed to update reaction.';
      }
    });
  }
}
