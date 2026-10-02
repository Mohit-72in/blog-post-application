import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PostService } from '../../services/post.service';
import { AuthService } from '../../services/auth.service';
import { Post, PagedResponse } from '../../models/post.model';
import { PostCardComponent } from '../post-card/post-card.component';

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, PostCardComponent],
  templateUrl: './post-list.component.html',
  styleUrl: './post-list.component.css'
})
export class PostListComponent implements OnInit {
  posts: Post[] = [];
  loading = true;
  errorMessage = '';

  filterAuthor: string | null = null;
  searchQuery: string | null = null;

  currentPage = 0;
  pageSize = 5;
  totalElements = 0;
  totalPages = 0;
  isFirst = true;
  isLast = true;
  hasNext = false;
  hasPrevious = false;

  readonly pageSizeOptions = [3, 5, 10, 20];

  constructor(
    private postService: PostService,
    public authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params: any) => {
      this.filterAuthor = params['author'] || null;
      this.searchQuery = params['search'] || null;
      this.currentPage = params['page'] !== undefined ? Math.max(0, parseInt(params['page'], 10) || 0) : 0;
      this.pageSize = params['size'] !== undefined ? Math.max(1, parseInt(params['size'], 10) || 5) : 5;
      this.fetchPosts();
    });
  }

  fetchPosts(): void {
    this.loading = true;
    this.errorMessage = '';
    this.postService.getPosts(
      this.filterAuthor || undefined,
      this.searchQuery || undefined,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (data: PagedResponse<Post>) => {
        this.posts = data.content || [];
        this.currentPage = data.pageNumber;
        this.pageSize = data.pageSize;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.isFirst = data.first;
        this.isLast = data.last;
        this.hasNext = data.hasNext;
        this.hasPrevious = data.hasPrevious;
        this.loading = false;
      },
      error: (err: any) => {
        this.errorMessage = err?.error?.message || 'Failed to load posts from server.';
        this.loading = false;
      }
    });
  }

  goToPage(page: number): void {
    if (page < 0 || (this.totalPages > 0 && page >= this.totalPages) || page === this.currentPage) {
      return;
    }
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: page },
      queryParamsHandling: 'merge'
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  onPageSizeChange(newSize: any): void {
    const size = parseInt(newSize, 10) || 5;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { size: size, page: 0 },
      queryParamsHandling: 'merge'
    });
  }

  getPages(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages - 1, start + maxVisible - 1);

    if (end - start + 1 < maxVisible) {
      start = Math.max(0, end - maxVisible + 1);
    }

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  clearSearch(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { search: null, page: 0 },
      queryParamsHandling: 'merge'
    });
  }

  clearAuthorFilter(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { author: null, page: 0 },
      queryParamsHandling: 'merge'
    });
  }

  clearAllFilters(): void {
    this.router.navigate(['/'], {
      queryParams: { page: 0 }
    });
  }

  onDeletePost(id: number): void {
    this.postService.deletePost(id).subscribe({
      next: () => {
        // Refresh current page
        this.fetchPosts();
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

