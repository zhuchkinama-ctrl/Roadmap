import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { AuthRequest } from '../../../shared/models';

// === CHUNK: LOGIN_COMPONENT_TEST [TEST] ===
// Описание: Тесты LoginComponent (Level 1 - Deterministic).
// Dependencies: Jasmine, Angular Testing Module

// [START_LOGIN_COMPONENT_TEST]
/*
 * ANCHOR: LOGIN_COMPONENT_TEST
 * PURPOSE: Тесты LoginComponent (Level 1 - Deterministic).
 *
 * @PreConditions:
 * - LoginComponent инициализирован
 * - AuthService и Router замокированы
 *
 * @PostConditions:
 * - все тесты проверяют поведение компонента
 *
 * @Invariants:
 * - тесты не зависят от реального API
 *
 * @SideEffects:
 * - none
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку валидации формы
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тесты
 */
describe('LoginComponent', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;
    let authService: jasmine.SpyObj<AuthService>;
    let router: jasmine.SpyObj<Router>;

    const mockAuthService = jasmine.createSpyObj('AuthService', ['login']);
    const mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ReactiveFormsModule, LoginComponent],
            providers: [
                FormBuilder,
                { provide: AuthService, useValue: mockAuthService },
                { provide: Router, useValue: mockRouter }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
        router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    // [START_LOGIN_COMPONENT_TEST_FORM_VALIDATION]
    /*
     * ANCHOR: LOGIN_COMPONENT_TEST_FORM_VALIDATION
     * PURPOSE: Проверка валидации формы.
     *
     * @PreConditions:
     * - форма создана
     *
     * @PostConditions:
     * - форма валидна при правильных данных
     * - форма невалидна при пустых данных
     *
     * @Invariants:
     * - username min 3, max 50
     * - password required
     *
     * @SideEffects:
     * - none
     *
     * @ForbiddenChanges:
     * - нельзя убрать валидацию
     *
     * @AllowedRefactorZone:
     * - можно изменить валидацию
     */
    it('should create form with validators', () => {
        const usernameControl = component.loginForm.get('username');
        const passwordControl = component.loginForm.get('password');

        expect(usernameControl).toBeTruthy();
        expect(passwordControl).toBeTruthy();

        // Проверка валидности пустой формы
        expect(component.loginForm.valid).toBeFalsy();

        // Проверка валидности формы с валидными данными
        component.loginForm.setValue({
            username: 'testuser',
            password: 'Password123!'
        });
        expect(component.loginForm.valid).toBeTruthy();
    });

    it('should require username', () => {
        const usernameControl = component.loginForm.get('username');
        usernameControl?.setValue('');
        expect(usernameControl?.valid).toBeFalsy();
    });

    it('should require password', () => {
        const passwordControl = component.loginForm.get('password');
        passwordControl?.setValue('');
        expect(passwordControl?.valid).toBeFalsy();
    });

    it('should validate username length (min 3)', () => {
        const usernameControl = component.loginForm.get('username');
        usernameControl?.setValue('ab');
        expect(usernameControl?.valid).toBeFalsy();
    });

    it('should validate username length (max 50)', () => {
        const usernameControl = component.loginForm.get('username');
        const longUsername = 'a'.repeat(51);
        usernameControl?.setValue(longUsername);
        expect(usernameControl?.valid).toBeFalsy();
    });
    // [END_LOGIN_COMPONENT_TEST_FORM_VALIDATION]

    // [START_LOGIN_COMPONENT_TEST_ONSUBMIT_SUCCESS]
    /*
     * ANCHOR: LOGIN_COMPONENT_TEST_ONSUBMIT_SUCCESS
     * PURPOSE: Проверка onSubmit при успешной аутентификации.
     *
     * @PreConditions:
     * - форма валидна
     * - AuthService.login возвращает успех
     *
     * @PostConditions:
     * - пользователь перенаправлен на /dashboard
     *
     * @Invariants:
     * - loading сбрасывается после успеха
     *
     * @SideEffects:
     * - navigation to /dashboard
     *
     * @ForbiddenChanges:
     * - нельзя убрать навигацию
     *
     * @AllowedRefactorZone:
     * - можно изменить логику обработки
     */
    it('should navigate to dashboard on successful login', () => {
        // Arrange
        component.loginForm.setValue({
            username: 'testuser',
            password: 'Password123!'
        });
        mockAuthService.login.and.returnValue(of({ token: 'token', refreshToken: 'refresh', user: { id: 1, username: 'testuser' } }));

        // Act
        component.onSubmit();

        // Assert
        expect(authService.login).toHaveBeenCalled();
        expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
        expect(component.loading).toBeFalsy();
        expect(component.errorMessage).toBe('');
    });
    // [END_LOGIN_COMPONENT_TEST_ONSUBMIT_SUCCESS]

    // [START_LOGIN_COMPONENT_TEST_ONSUBMIT_ERROR]
    /*
     * ANCHOR: LOGIN_COMPONENT_TEST_ONSUBMIT_ERROR
     * PURPOSE: Проверка onSubmit при ошибке аутентификации.
     *
     * @PreConditions:
     * - форма валидна
     * - AuthService.login возвращает ошибку
     *
     * @PostConditions:
     * - errorMessage устанавливается
     * - loading сбрасывается
     *
     * @Invariants:
     * - пользователь не навигируется
     *
     * @SideEffects:
     * - none
     *
     * @ForbiddenChanges:
     * - нельзя убрать обработку ошибки
     *
     * @AllowedRefactorZone:
     * - можно изменить сообщение об ошибке
     */
    it('should show error on failed login', () => {
        // Arrange
        component.loginForm.setValue({
            username: 'testuser',
            password: 'Password123!'
        });
        const errorResponse = {
            error: { message: 'Invalid credentials' }
        };
        mockAuthService.login.and.returnValue(throwError(() => errorResponse));

        // Act
        component.onSubmit();

        // Assert
        expect(authService.login).toHaveBeenCalled();
        expect(router.navigate).not.toHaveBeenCalled();
        expect(component.loading).toBeFalsy();
        expect(component.errorMessage).toBe('Invalid credentials');
    });
    // [END_LOGIN_COMPONENT_TEST_ONSUBMIT_ERROR]

    // [START_LOGIN_COMPONENT_TEST_ONSUBMIT_INVALID_FORM]
    /*
     * ANCHOR: LOGIN_COMPONENT_TEST_ONSUBMIT_INVALID_FORM
     * PURPOSE: Проверка onSubmit при невалидной форме.
     *
     * @PreConditions:
     * - форма невалидна
     *
     * @PostConditions:
     * - метод не вызывает AuthService
     *
     * @Invariants:
     * - пользователь не пытается войти с невалидными данными
     *
     * @SideEffects:
     * - none
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку валидности
     *
     * @AllowedRefactorZone:
     * - можно изменить логику проверки
     */
    it('should not submit if form is invalid', () => {
        // Arrange
        component.loginForm.setValue({
            username: '', // Invalid
            password: ''
        });

        // Act
        component.onSubmit();

        // Assert
        expect(authService.login).not.toHaveBeenCalled();
        expect(router.navigate).not.toHaveBeenCalled();
    });
    // [END_LOGIN_COMPONENT_TEST_ONSUBMIT_INVALID_FORM]

    // [START_LOGIN_COMPONENT_TEST_GOTOREGISTER]
    /*
     * ANCHOR: LOGIN_COMPONENT_TEST_GOTOREGISTER
     * PURPOSE: Проверка goToRegister.
     *
     * @PreConditions:
     * - компонент инициализирован
     *
     * @PostConditions:
     * - навигация на /register
     *
     * @Invariants:
     * - none
     *
     * @SideEffects:
     * - navigation to /register
     *
     * @ForbiddenChanges:
     * - нельзя убрать навигацию
     *
     * @AllowedRefactorZone:
     * - можно изменить маршрут
     */
    it('should navigate to register page', () => {
        // Act
        component.goToRegister();

        // Assert
        expect(router.navigate).toHaveBeenCalledWith(['/register']);
    });
    // [END_LOGIN_COMPONENT_TEST_GOTOREGISTER]
});
// [END_LOGIN_COMPONENT_TEST]
// === END_CHUNK: LOGIN_COMPONENT_TEST ===
