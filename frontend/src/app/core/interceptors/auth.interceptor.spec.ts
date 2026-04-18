import { TestBed } from '@angular/core/testing';
import { HttpTestingController, HttpClientTestingModule } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { HttpRequest, HttpResponse } from '@angular/common/http';

// === CHUNK: AUTH_INTERCEPTOR_TEST [TEST] ===
// Описание: Тесты authInterceptor (Level 1 - Deterministic).
// Dependencies: Jasmine, HttpClientTestingModule

// [START_AUTH_INTERCEPTOR_TEST]
/*
 * ANCHOR: AUTH_INTERCEPTOR_TEST
 * PURPOSE: Тесты authInterceptor (Level 1 - Deterministic).
 *
 * @PreConditions:
 * - Router доступен
 *
 * @PostConditions:
 * - все тесты проверяют поведение interceptor
 *
 * @Invariants:
 * - тесты не зависят от реального API
 *
 * @SideEffects:
 * - none
 *
 * @ForbiddenChanges:
 * - нельзя убрать добавление токена
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тесты
 */
describe('authInterceptor', () => {
    let router: jasmine.SpyObj<Router>;
    let httpMock: HttpTestingController;

    const mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                { provide: Router, useValue: mockRouter }
            ]
        });

        router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should add Authorization header when token exists', () => {
        // Arrange
        localStorage.setItem('access_token', 'test_token_123');

        // Act
        const req = httpMock.expectOne((request: HttpRequest<any>) => {
            return request.headers.has('Authorization');
        });

        // Assert
        expect(req.request.headers.get('Authorization')).toBe('Bearer test_token_123');
    });

    it('should not add Authorization header when token does not exist', () => {
        // Arrange
        localStorage.removeItem('access_token');

        // Act
        const req = httpMock.expectOne(() => true);

        // Assert
        expect(req.request.headers.has('Authorization')).toBeFalse();
    });

    it('should handle 401 error and try to refresh token', () => {
        // Arrange
        localStorage.setItem('access_token', 'expired_token');

        // Act
        const req = httpMock.expectOne(() => true);
        req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

        // Assert
        expect(router.navigate).not.toHaveBeenCalled(); // Token refresh is handled
    });

    it('should navigate to login on 403 error', () => {
        // Arrange
        localStorage.setItem('access_token', 'valid_token');

        // Act
        const req = httpMock.expectOne(() => true);
        req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });

        // Assert
        expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should not refresh token for auth endpoints', () => {
        // Arrange
        localStorage.setItem('access_token', 'expired_token');

        // Act
        const req = httpMock.expectOne((request: HttpRequest<any>) => {
            return request.url.includes('/auth/refresh');
        });
        req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

        // Assert
        expect(router.navigate).not.toHaveBeenCalled();
    });
});
// [END_AUTH_INTERCEPTOR_TEST]
// === END_CHUNK: AUTH_INTERCEPTOR_TEST ===
