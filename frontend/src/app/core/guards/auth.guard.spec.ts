import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { authGuard } from './auth.guard';
import { logLine } from '../lib/log';

// === CHUNK: AUTH_GUARD_TEST [TEST] ===
// Описание: Тесты authGuard (Level 1 - Deterministic).
// Dependencies: Jasmine, Angular Testing Module

// [START_AUTH_GUARD_TEST]
/*
 * ANCHOR: AUTH_GUARD_TEST
 * PURPOSE: Тесты authGuard (Level 1 - Deterministic).
 *
 * @PreConditions:
 * - AuthService и Router доступны
 *
 * @PostConditions:
 * - все тесты проверяют поведение guard
 *
 * @Invariants:
 * - тесты не зависят от реального API
 *
 * @SideEffects:
 * - none
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку аутентификации
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тесты
 */
describe('authGuard', () => {
    let authService: jasmine.SpyObj<AuthService>;
    let router: jasmine.SpyObj<Router>;

    const mockAuthService = jasmine.createSpyObj('AuthService', ['isAuthenticated']);
    const mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                { provide: AuthService, useValue: mockAuthService },
                { provide: Router, useValue: mockRouter }
            ]
        });

        authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
        router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    });

    it('should allow access when user is authenticated', () => {
        // Arrange
        authService.isAuthenticated.and.returnValue(true);

        // Act
        const result = authGuard({} as any, { url: '/dashboard' } as any);

        // Assert
        expect(authService.isAuthenticated).toHaveBeenCalled();
        expect(result).toBeTrue();
        expect(router.navigate).not.toHaveBeenCalled();
    });

    it('should deny access when user is not authenticated', () => {
        // Arrange
        authService.isAuthenticated.and.returnValue(false);

        // Act
        const result = authGuard({} as any, { url: '/dashboard' } as any);

        // Assert
        expect(authService.isAuthenticated).toHaveBeenCalled();
        expect(result).toBeFalse();
        expect(router.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { returnUrl: '/dashboard' } });
    });

    it('should save return URL for redirect after login', () => {
        // Arrange
        authService.isAuthenticated.and.returnValue(false);

        // Act
        authGuard({} as any, { url: '/dashboard/settings' } as any);

        // Assert
        expect(router.navigate).toHaveBeenCalledWith(['/login'], {
            queryParams: { returnUrl: '/dashboard/settings' }
        });
    });
});
// [END_AUTH_GUARD_TEST]
// === END_CHUNK: AUTH_GUARD_TEST ===
