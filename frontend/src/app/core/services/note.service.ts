import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  NoteDto,
  NoteTreeNodeDto,
  CreateNoteRequest,
  UpdateNoteRequest,
  MoveNoteRequest
} from '../../shared/models';

@Injectable({
  providedIn: 'root'
})
export class NoteService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getNotesTree(trackId: number): Observable<NoteTreeNodeDto[]> {
    return this.http.get<NoteTreeNodeDto[]>(`${this.apiUrl}/tracks/${trackId}/notes/tree`);
  }

  getNote(id: number): Observable<NoteDto> {
    return this.http.get<NoteDto>(`${this.apiUrl}/notes/${id}`);
  }

  createNote(trackId: number, request: CreateNoteRequest): Observable<NoteDto> {
    return this.http.post<NoteDto>(`${this.apiUrl}/tracks/${trackId}/notes`, request);
  }

  updateNote(id: number, request: UpdateNoteRequest): Observable<NoteDto> {
    return this.http.put<NoteDto>(`${this.apiUrl}/notes/${id}`, request);
  }

  deleteNote(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/notes/${id}`);
  }

  moveNote(id: number, request: MoveNoteRequest): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/notes/${id}/move`, request);
  }
}
