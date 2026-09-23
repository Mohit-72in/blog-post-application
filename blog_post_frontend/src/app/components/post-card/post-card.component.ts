import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Post } from '../../models/post.model';

@Component({
  selector: 'app-post-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './post-card.component.html',
  styleUrl: './post-card.component.css'
})
export class PostCardComponent {
  @Input({ required: true }) post!: Post;
  @Output() delete = new EventEmitter<number>();
  @Output() react = new EventEmitter<number>();

  onDelete(): void {
    if (this.post.id && confirm('Are you sure you want to delete this post?')) {
      this.delete.emit(this.post.id);
    }
  }

  onReact(): void {
    if (this.post.id) {
      this.react.emit(this.post.id);
    }
  }
}
