import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { RegisterRequest } from '../../../shared/models';

// === CHUNK: REGISTER_COMPONENT_TEST [TEST] ===
// Описание: Тесты RegisterComponent (Level 1 - Deterministic).
// Dependencies: Jasmine, Angular Testing Module

// [START_REGISTER_COMPONENT_TEST]
/*
 * ANCHOR: REGISTER_COMPONENT_TEST
 * PURPOSE: Тесты RegisterComponent (Level 1 - Deterministic).
 *
 * @PreConditions:
 * - RegisterComponent инициализирован
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
describe('RegisterComponent', () => {
    let component: RegisterComponent;
    let fixture: ComponentFixture<RegisterComponent>;
    let authService: jasmine.SpyObj<AuthService>;
    let router: jasmine.SpyObj<Router>;

    const mockAuthService = jasmine.createSpyObj('AuthService', ['register']);
    const mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ReactiveFormsModule, RegisterComponent],
            providers: [
                FormBuilder,
                { provide: AuthService, useValue: mockAuthService },
                { provide: Router, useValue: mockRouter }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(RegisterComponent);
        component = fixture.componentInstance;
        authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
        router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    // [START_REGISTER_COMPONENT_TEST_FORM_VALIDATION]
    /*
     * ANCHOR: REGISTER_COMPONENT_TEST_FORM_VALIDATION
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
     * - username min 3, max 50, только латиница и цифры
     * - email валидный email
     * - password min 8, с заглавной буквой, цифрой и специальным символом
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
        const usernameControl = component.registerForm.get('username');
        const emailControl = component.registerForm.get('email');
        const passwordControl = component.registerForm.get('password');
        const confirmPasswordControl = component.registerForm.get('confirmPassword');

        expect(usernameControl).toBeTruthy();
        expect(emailControl).toBeTruthy();
        expect(passwordControl).toBeTruthy();
        expect(confirmPasswordControl).toBeTruthy();

        // Проверка валидности пустой формы
        expect(component.registerForm.valid).toBeFalsy();

        // Проверка валидности формы с валидными данными
        component.registerForm.setValue({
            username: 'testuser',
            email: 'test@example.com',
            password: 'Password123!',
            confirmPassword: 'Password123!'
        });
        expect(component.registerForm.valid).toBeTruthy();
    });

    it('should require username', () => {
        const usernameControl = component.registerForm.get('username');
        usernameControl?.setValue('');
        expect(usernameControl?.valid).toBeFalsy();
    });

    it('should require email', () => {
        const emailControl = component.registerForm.get('email');
        emailControl?.setValue('');
        expect(emailControl?.valid).toBeFalsy();
    });

    it('should require password', () => {
        const passwordControl = component.registerForm.get('password');
        passwordControl?.setValue('');
        expect(passwordControl?.valid).toBeFalsy();
    });

    it('should require confirmPassword', () => {
        const confirmPasswordControl = component.registerForm.get('confirmPassword');
        confirmPasswordControl?.setValue('');
        expect(confirmPasswordControl?.valid).toBeFalsy();
    });

    it('should validate username length (min 3)', () => {
        const usernameControl = component.registerForm.get('username');
        usernameControl?.setValue('ab');
        expect(usernameControl?.valid).toBeFalsy();
    });

    it('should validate username length (max 50)', () => {
        const usernameControl = component.registerForm.get('username');
        const longUsername = 'a'.repeat(51);
        usernameControl?.setValue(longUsername);
        expect(usernameControl?.valid).toBeFalsy();
    });

    it('should validate username pattern (only letters and numbers)', () => {
        const usernameControl = component.registerForm.get('username');
        usernameControl?.setValue('test_user'); // underscore not allowed
        expect(usernameControl?.valid).toBeFalsy();
    });

    it('should validate email format', () => {
        const emailControl = component.registerForm.get('email');
        emailControl?.setValue('invalid-email');
        expect(emailControl?.valid).toBeFalsy();
    });

    it('should validate password length (min 8)', () => {
        const passwordControl = component.registerForm.get('password');
        passwordControl?.setValue('Short1!');
        expect(passwordControl?.valid).toBeFalsy();
    });

    it('should validate password contains uppercase', () => {
        const passwordControl = component.registerForm.get('password');
        passwordControl?.setValue('password123!');
        expect(passwordControl?.valid).toBeFalsy();
    });

    it('should validate password contains digit', () => {
        const passwordControl = component.registerForm.get('password');
        passwordControl?.setValue('Password!');
        expect(passwordControl?.valid).toBeFalsy();
    });

    it('should validate password contains special character', () => {
        const passwordControl = component.registerForm.get('password');
        passwordControl?.setValue('Password123');
        expect(passwordControl?.valid).toBeFalsy();
    });

    it('should validate password match', () => {
        component.registerForm.setValue({
            username: 'testuser',
            email: 'test@example.com',
            password: 'Password123!',
            confirmPassword: 'Password123!'
        });
        expect(component.registerForm.valid).toBeTruthy();

        component.registerForm.setValue({
            username: 'testuser',
            email: 'test@example.com',
            password: 'Password123!',
            confirmPassword: 'Password1234!' // Different
        });
        expect(component.registerForm.errors?.['passwordMismatch']).toBeTruthy();
    });
    // [END_REGISTER_COMPONENT_TEST_FORM_VALIDATION]

    // [START_REGISTER_COMPONENT_TEST_ONSUBMIT_SUCCESS]
    /*
     * ANCHOR: REGISTER_COMPONENT_TEST_ONSUBMIT_SUCCESS
     * PURPOSE: Проверка onSubmit при успешной регистрации.
     *
     * @PreConditions:
     * - форма валидна
     * - AuthService.register возвращает успех
     *
     * @PostConditions:
     * - successMessage устанавливается
     * - loading сбрасывается
     * - навигация на /login через 2 секунды
     *
     * @Invariants:
     * - none
     *
     * @SideEffects:
     * - none
     *
     * @ForbiddenChanges:
     * - нельзя убрать обработку успеха
     *
     * @AllowedRefactorZone:
     * - можно изменить логику обработки
     */
    it('should show success and navigate to login on successful registration', (done) => {
        // Arrange
        component.registerForm.setValue({
            username: 'testuser',
            email: 'test@example.com',
            password: 'Password123!',
            confirmPassword: 'Password123!'
        });
        mockAuthService.register.and.returnValue(of({ token: 'token', refreshToken: 'refresh', user: { id: 1, username: 'testuser' } }));

        // Act
        component.onSubmit();

        // Assert
        expect(authService.register).toHaveBeenCalled();
        expect(component.successMessage).toBe('Регистрация успешна! Теперь вы можете войти.');
        expect(component.loading).toBeFalsy();

        // Проверка навигации через таймаут
        setTimeout(() => {
            expect(router.navigate).toHaveBeenCalledWith(['/login']);
            done();
        }, 2100);
    });
    // [END_REGISTER_COMPONENT_TEST_ONSUBMIT_SUCCESS]

    // [START_REGISTER_COMPONENT_TEST_ONSUBMIT_ERROR]
    /*
     * ANCHOR: REGISTER_COMPONENT_TEST_ONSUBMIT_ERROR
     * PURPOSE: Проверка onSubmit при ошибке регистрации.
     *
     * @PreConditions:
     * - форма валидна
     * - AuthService.register возвращает ошибку
     *
     * @PostConditions:
     * - errorMessage устанавливается
     * - loading сбрасывается
     *
     * @Invariants:
     * - none
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
    it('should show error on failed registration', () => {
        // Arrange
        component.registerForm.setValue({
            username: 'testuser',
            email: 'test@example.com',
            password: 'Password123!',
            confirmPassword: 'Password123!'
        });
        const errorResponse = {
            error: { message: 'User already exists' },
            status: 409
        };
        mockAuthService.register.and.returnValue(throwError(() => errorResponse));

        // Act
        component.onSubmit();

        // Assert
        expect(authService.register).toHaveBeenCalled();
        expect(router.navigate).not.toHaveBeenCalled();
        expect(component.loading).toBeFalsy();
        expect(component.errorMessage).toBe('User already exists');
    });

    it('should show error for duplicate user (409)', () => {
        // Arrange
        component.registerForm.setValue({
            username: 'testuser',
            email: 'test@example.com',
            password: 'Password123!',
            confirmPassword: 'Password123!'
        });
        const errorResponse = {
            error: {},
            status: 409
        };
        mockAuthService.register.and.returnValue(throwError(() => errorResponse));

        // Act
        component.onSubmit();

        // Assert
        expect(component.errorMessage).toBe('Пользователь с таким именем или email уже существует');
    });

    it('should show error for validation error (400)', () => {
        // Arrange
        component.registerForm.setValue({
            username: 'testuser',
            email: 'test@example.com',
            password: 'Password123!',
            confirmPassword: 'Password123!'
        });
        const errorResponse = {
            error: {},
            status: 400
        };
        mockAuthService.register.and.returnValue(throwError(() => errorResponse));

        // Act
        component.onSubmit();

        // Assert
        expect(component.errorMessage).toBe('Некорректные данные регистрации. Проверьте введенные данные.');
    });
    // [END_REGISTER_COMPONENT_TEST_ONSUBMIT_ERROR]

    // [START_REGISTER_COMPONENT_TEST_ONSUBMIT_INVALID_FORM]
    /*
     * ANCHOR: REGISTER_COMPONENT_TEST_ONSUBMIT_INVALID_FORM
     * PURPOSE: Проверка onSubmit при невалидной форме.
     *
     * @PreConditions:
     * - форма невалидна
     *
     * @PostConditions:
     * - метод не вызывает AuthService
     *
     * @Invariants:
     * - none
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
        component.registerForm.setValue({
            username: '', // Invalid
            email: '',
            password: '',
            confirmPassword: ''
        });

        // Act
        component.onSubmit();

        // Assert
        expect(authService.register).not.toHaveBeenCalled();
        expect(router.navigate).not.toHaveBeenCalled();
    });
    // [END_REGISTER_COMPONENT_TEST_ONSUBMIT_INVALID_FORM]
});
// [END_REGISTER_COMPONENT_TEST]
// === END_CHUNK: REGISTER_COMPONENT_TEST ===
