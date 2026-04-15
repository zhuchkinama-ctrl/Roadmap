import { HttpInterceptorFn } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';
import { inject } from '@angular/core';
import { Router } from '@angular/router';

const isRefreshing = false;
const refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('access_token');

  if (token) {
    req = addToken(req, token);
  }

  return next(req).pipe(
    catchError((error: any) => {
      if (error.status === 401 && !req.url.includes('/auth/refresh')) {
        return handle401Error(req, next, router);
      }
      if (error.status === 403) {
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};

function addToken(request: any, token: string): any {
  return request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
}

function handle401Error(request: any, next: any, router: Router): Observable<any> {
  if (!isRefreshing) {
    const refreshToken = localStorage.getItem('refresh_token');

    if (refreshToken) {
      return refreshTokenCall(refreshToken).pipe(
        switchMap((response: any) => {
          localStorage.setItem('access_token', response.token);
          localStorage.setItem('refresh_token', response.refreshToken);
          refreshTokenSubject.next(response.token);
          return next(addToken(request, response.token));
        }),
        catchError((error) => {
          logout(router);
          return throwError(() => error);
        })
      );
    } else {
      logout(router);
      return throwError(() => new Error('No refresh token available'));
    }
  } else {
    return refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(token => next(addToken(request, token)))
    );
  }
}

function refreshTokenCall(refreshToken: string): Observable<any> {
  const apiUrl = 'http://localhost:8080/api/v1';
  return new Observable(observer => {
    fetch(`${apiUrl}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ refreshToken })
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('Refresh failed');
      }
      return response.json();
    })
    .then(data => {
      observer.next(data);
      observer.complete();
    })
    .catch(error => {
      observer.error(error);
    });
  });
}

function logout(router: Router): void {
  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
  router.navigate(['/login']);
}
