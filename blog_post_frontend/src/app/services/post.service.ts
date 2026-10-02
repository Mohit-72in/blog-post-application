import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Post, Comment, CreatePostPayload, UpdatePostPayload, PagedResponse } from '../models/post.model';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private apiUrl = `${environment.apiUrl}/posts`;

  constructor(private http: HttpClient) {}

  getPosts(author?: string, search?: string, page: number = 0, size: number = 5): Observable<PagedResponse<Post>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (author && author.trim()) {
      params = params.set('author', author.trim());
    }
    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }

    return this.http.get<PagedResponse<Post>>(this.apiUrl, { params });
  }

  getPostById(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.apiUrl}/${id}`);
  }

  createPost(payload: CreatePostPayload): Observable<Post> {
    return this.http.post<Post>(this.apiUrl, payload);
  }

  updatePost(id: number, payload: UpdatePostPayload): Observable<Post> {
    return this.http.put<Post>(`${this.apiUrl}/${id}`, payload);
  }

  deletePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  reactToPost(id: number): Observable<Post> {
    return this.http.patch<Post>(`${this.apiUrl}/${id}/react`, {});
  }

  getComments(postId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.apiUrl}/${postId}/comments`);
  }

  addComment(postId: number, text: string): Observable<Comment> {
    return this.http.post<Comment>(`${this.apiUrl}/${postId}/comments`, { text });
  }
}
