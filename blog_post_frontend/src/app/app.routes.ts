import { Routes } from '@angular/router';
import { PostListComponent } from './components/post-list/post-list.component';
import { PostFormComponent } from './components/post-form/post-form.component';

export const routes: Routes = [
  { path: '', component: PostListComponent },
  { path: 'create', component: PostFormComponent },
  { path: 'edit/:id', component: PostFormComponent },
  { path: '**', redirectTo: '' }
];
