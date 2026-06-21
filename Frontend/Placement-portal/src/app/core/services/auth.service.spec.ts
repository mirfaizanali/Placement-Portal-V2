import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

/**
 * Locks B2 — the AuthResponse.email returned by the backend must end up on
 * the AuthService.currentUser() signal, instead of being silently discarded.
 */
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('login() stores the email from AuthResponse into currentUser()', () => {
    service.login('ada@example.com', 'hunter2').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({
      success: true,
      message: 'ok',
      data: {
        accessToken: 'jwt',
        tokenType: 'Bearer',
        expiresIn: 900000,
        role: 'STUDENT',
        userId: 'u-1',
        fullName: 'Ada Lovelace',
        email: 'ada@example.com'
      }
    });

    expect(service.currentUser()?.email).toBe('ada@example.com');
    expect(service.isAuthenticated()).toBeTrue();
    expect(service.getAccessToken()).toBe('jwt');
  });

  it('login() falls back to empty email when backend omits it (back-compat)', () => {
    service.login('ada@example.com', 'hunter2').subscribe();

    httpMock.expectOne(`${environment.apiUrl}/api/auth/login`).flush({
      success: true, message: 'ok',
      data: {
        accessToken: 'jwt', tokenType: 'Bearer', expiresIn: 900000,
        role: 'STUDENT', userId: 'u-1', fullName: 'Ada Lovelace'
      }
    });

    expect(service.currentUser()?.email).toBe('');
  });
});
