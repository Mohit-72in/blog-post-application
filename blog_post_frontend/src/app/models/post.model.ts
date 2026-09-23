export interface Post {
  id?: number;
  title: string;
  body: string;
  tags: string[];
  author?: string;
  reactions: number;
  createdAt?: string;
  comments?: Comment[];
}

export interface Comment {
  id?: number;
  text: string;
}

export interface CreatePostPayload {
  title: string;
  body: string;
  tags: string[];
  author?: string;
  firstComment?: string;
}

export interface UpdatePostPayload {
  title: string;
  body: string;
  tags: string[];
}
