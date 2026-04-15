import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  TrackDto,
  TrackSummaryDto,
  TrackPageResponse,
  CreateTrackRequest,
  UpdateTrackRequest,
  GrantPermissionRequest,
  PermissionDto
} from '../../shared/models';

@Injectable({
  providedIn: 'root'
})
export class TrackService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getTracks(page: number = 0, size: number = 10, sort: string = 'updatedAt,desc'): Observable<TrackPageResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<TrackPageResponse>(`${this.apiUrl}/tracks`, { params });
  }

  getTrack(id: number): Observable<TrackDto> {
    return this.http.get<TrackDto>(`${this.apiUrl}/tracks/${id}`);
  }

  createTrack(request: CreateTrackRequest): Observable<TrackDto> {
    return this.http.post<TrackDto>(`${this.apiUrl}/tracks`, request);
  }

  updateTrack(id: number, request: UpdateTrackRequest): Observable<TrackDto> {
    return this.http.put<TrackDto>(`${this.apiUrl}/tracks/${id}`, request);
  }

  deleteTrack(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/tracks/${id}`);
  }

  grantPermission(trackId: number, request: GrantPermissionRequest): Observable<PermissionDto> {
    return this.http.post<PermissionDto>(`${this.apiUrl}/tracks/${trackId}/permissions`, request);
  }
}
