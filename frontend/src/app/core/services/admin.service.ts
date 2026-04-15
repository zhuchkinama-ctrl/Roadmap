import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserAdminDto } from '../../shared/models';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getUsers(page: number = 0, size: number = 10): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(`${this.apiUrl}/admin/users`, { params });
  }

  lockUser(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/admin/users/${id}/lock`, {});
  }

  unlockUser(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/admin/users/${id}/lock`, {});
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/users/${id}`);
  }

  changeUserRole(id: number, role: 'USER' | 'ADMIN'): Observable<UserAdminDto> {
    return this.http.patch<UserAdminDto>(`${this.apiUrl}/admin/users/${id}/role`, { role });
  }
}
