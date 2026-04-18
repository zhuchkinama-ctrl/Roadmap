import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { AuthRequest, RegisterRequest, AuthResponse, UserDto } from '../../shared/models';

// === CHUNK: AUTH_SERVICE_TEST [TEST] ===
// Описание: Тесты AuthService (Level 1 - Deterministic).
// Dependencies: Jasmine, HttpClientTestingModule

// [START_AUTH_SERVICE_TEST]
/*
 * ANCHOR: AUTH_SERVICE_TEST
 * PURPOSE: Тесты AuthService (Level 1 - Deterministic).
 *
 * @PreConditions:
 * - AuthService инициализирован
 * - HttpClient замокирован
 *
 * @PostConditions:
 * - все тесты проверяют поведение сервиса
 *
 * @Invariants:
 * - тесты не зависят от реального API
 *
 * @SideEffects:
 * - none
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку localStorage
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тесты
 */
describe('AuthService', () => {
    let service: AuthService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [AuthService]
        });

        service = TestBed.inject(AuthService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should create', () => {
        expect(service).toBeTruthy();
    });

    // [START_AUTH_SERVICE_TEST_LOGIN]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_LOGIN
     * PURPOSE: Проверка метода login.
     *
     * @PreConditions:
     * - credentials валидны
     *
     * @PostConditions:
     * - при успехе: токены сохранены в localStorage
     *
     * @Invariants:
     * - токены сохраняются только при успешном ответе
     *
     * @SideEffects:
     * - запись в localStorage
     *
     * @ForbiddenChanges:
     * - нельзя убрать сохранение токенов
     *
     * @AllowedRefactorZone:
     * - можно изменить логику обработки
     */
    it('should login successfully and save tokens', () => {
        // Arrange
        const credentials: AuthRequest = {
            username: 'testuser',
            password: 'Password123!'
        };
        const mockResponse: AuthResponse = {
            token: 'access_token_123',
            refreshToken: 'refresh_token_123',
            user: { id: 1, username: 'testuser', role: 'USER', enabled: true, createdAt: new Date() }
        };

        // Act
        service.login(credentials).subscribe(response => {
            expect(response).toEqual(mockResponse);
        });

        // Assert
        const req = httpMock.expectOne(`${service['apiUrl']}/auth/login`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(credentials);

        req.flush(mockResponse);

        expect(localStorage.getItem('access_token')).toBe('access_token_123');
        expect(localStorage.getItem('refresh_token')).toBe('refresh_token_123');
    });
    // [END_AUTH_SERVICE_TEST_LOGIN]

    // [START_AUTH_SERVICE_TEST_LOGIN_ERROR]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_LOGIN_ERROR
     * PURPOSE: Проверка метода login при ошибке.
     *
     * @PreConditions:
     * - credentials невалидны
     *
     * @PostConditions:
     * - выбрасывается ошибка
     *
     * @Invariants:
     * - токены не сохраняются при ошибке
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
    it('should throw error on login failure', () => {
        // Arrange
        const credentials: AuthRequest = {
            username: 'testuser',
            password: 'WrongPassword'
        };

        // Act & Assert
        service.login(credentials).subscribe({
            next: () => fail('Should have thrown error'),
            error: (error) => {
                expect(error.status).toBe(401);
            }
        });

        const req = httpMock.expectOne(`${service['apiUrl']}/auth/login`);
        expect(req.request.method).toBe('POST');

        req.flush('Invalid credentials', { status: 401, statusText: 'Unauthorized' });
    });
    // [END_AUTH_SERVICE_TEST_LOGIN_ERROR]

    // [START_AUTH_SERVICE_TEST_REGISTER]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_REGISTER
     * PURPOSE: Проверка метода register.
     *
     * @PreConditions:
     * - data валидны
     *
     * @PostConditions:
     * - при успехе: возвращается UserDto
     *
     * @Invariants:
     * - none
     *
     * @SideEffects:
     * - none
     *
     * @ForbiddenChanges:
     * - нельзя убрать HTTP запрос
     *
     * @AllowedRefactorZone:
     * - можно изменить логику обработки
     */
    it('should register successfully', () => {
        // Arrange
        const registerData: RegisterRequest = {
            username: 'newuser',
            email: 'newuser@example.com',
            password: 'Password123!'
        };
        const mockResponse: UserDto = {
            id: 1,
            username: 'newuser',
            role: 'USER',
            enabled: true,
            createdAt: new Date()
        };

        // Act
        service.register(registerData).subscribe(response => {
            expect(response).toEqual(mockResponse);
        });

        // Assert
        const req = httpMock.expectOne(`${service['apiUrl']}/auth/register`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(registerData);

        req.flush(mockResponse);
    });
    // [END_AUTH_SERVICE_TEST_REGISTER]

    // [START_AUTH_SERVICE_TEST_REGISTER_ERROR]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_REGISTER_ERROR
     * PURPOSE: Проверка метода register при ошибке.
     *
     * @PreConditions:
     * - data невалидны
     *
     * @PostConditions:
     * - выбрасывается ошибка
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
    it('should throw error on registration failure', () => {
        // Arrange
        const registerData: RegisterRequest = {
            username: 'newuser',
            email: 'newuser@example.com',
            password: 'Password123!'
        };

        // Act & Assert
        service.register(registerData).subscribe({
            next: () => fail('Should have thrown error'),
            error: (error) => {
                expect(error.status).toBe(409);
            }
        });

        const req = httpMock.expectOne(`${service['apiUrl']}/auth/register`);
        expect(req.request.method).toBe('POST');

        req.flush('User already exists', { status: 409, statusText: 'Conflict' });
    });
    // [END_AUTH_SERVICE_TEST_REGISTER_ERROR]

    // [START_AUTH_SERVICE_TEST_LOGOUT]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_LOGOUT
     * PURPOSE: Проверка метода logout.
     *
     * @PreConditions:
     * - пользователь аутентифицирован
     *
     * @PostConditions:
     * - токены удалены из localStorage
     * - currentUserSubject сброшен
     *
     * @Invariants:
     * - после logout пользователь неаутентифицирован
     *
     * @SideEffects:
     * - удаление из localStorage
     *
     * @ForbiddenChanges:
     * - нельзя оставить токены после logout
     *
     * @AllowedRefactorZone:
     * - можно добавить очистку других данных
     */
    it('should logout successfully and remove tokens', () => {
        // Arrange
        localStorage.setItem('access_token', 'access_token_123');
        localStorage.setItem('refresh_token', 'refresh_token_123');

        // Act
        service.logout().subscribe(() => {
            expect(localStorage.getItem('access_token')).toBeNull();
            expect(localStorage.getItem('refresh_token')).toBeNull();
        });

        // Assert
        const req = httpMock.expectOne(`${service['apiUrl']}/auth/logout`);
        expect(req.request.method).toBe('POST');

        req.flush({});
    });
    // [END_AUTH_SERVICE_TEST_LOGOUT]

    // [START_AUTH_SERVICE_TEST_ISAUTHENTICATED]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_ISAUTHENTICATED
     * PURPOSE: Проверка метода isAuthenticated.
     *
     * @PreConditions:
     * - токен может быть в localStorage
     *
     * @PostConditions:
     * - возвращает true/false в зависимости от наличия токена
     *
     * @Invariants:
     * - метод проверяет наличие access_token
     *
     * @SideEffects:
     * - none
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку токена
     *
     * @AllowedRefactorZone:
     * - можно изменить логику проверки
     */
    it('should return true if user is authenticated', () => {
        // Arrange
        localStorage.setItem('access_token', 'access_token_123');

        // Act
        const isAuthenticated = service.isAuthenticated();

        // Assert
        expect(isAuthenticated).toBe(true);
    });

    it('should return false if user is not authenticated', () => {
        // Arrange
        localStorage.removeItem('access_token');

        // Act
        const isAuthenticated = service.isAuthenticated();

        // Assert
        expect(isAuthenticated).toBe(false);
    });
    // [END_AUTH_SERVICE_TEST_ISAUTHENTICATED]

    // [START_AUTH_SERVICE_TEST_GETCURRENTUSER]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_GETCURRENTUSER
     * PURPOSE: Проверка метода getCurrentUser.
     *
     * @PreConditions:
     * - пользователь может быть в currentUserSubject
     *
     * @PostConditions:
     * - возвращает UserDto или null
     *
     * @Invariants:
     * - метод возвращает текущего пользователя из subject
     *
     * @SideEffects:
     * - none
     *
     * @ForbiddenChanges:
     * - нельзя убрать возврат пользователя
     *
     * @AllowedRefactorZone:
     * - можно изменить логику получения
     */
    it('should return current user', () => {
        // Arrange
        const mockUser: UserDto = {
            id: 1,
            username: 'testuser',
            role: 'USER',
            enabled: true,
            createdAt: new Date()
        };
        service['currentUserSubject'].next(mockUser);

        // Act
        service.getCurrentUser().subscribe(user => {
            expect(user).toEqual(mockUser);
        });
    });

    it('should return null if no current user', () => {
        // Arrange
        service['currentUserSubject'].next(null);

        // Act
        service.getCurrentUser().subscribe(user => {
            expect(user).toBeNull();
        });
    });
    // [END_AUTH_SERVICE_TEST_GETCURRENTUSER]
});
// [END_AUTH_SERVICE_TEST]
// === END_CHUNK: AUTH_SERVICE_TEST ===
