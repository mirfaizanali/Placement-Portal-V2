import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpHandlerFn, HttpRequest, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { jwtInterceptor } from './jwt.interceptor';
import { AuthService } from '../services/auth.service';
import { of } from 'rxjs';

describe('jwtInterceptor', () => {
  let http: HttpClient;
  let mock: HttpTestingController;
  let auth: { getAccessToken: () => string | null };

  beforeEach(() => {
    auth = { getAccessToken: () => null };
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth }
      ]
    });
    http = TestBed.inject(HttpClient);
    mock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => mock.verify());

  it('attaches Authorization header when a token is present', () => {
    auth.getAccessToken = () => 'abc.def.ghi';
    http.get('/test').subscribe();
    const req = mock.expectOne('/test');
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc.def.ghi');
    req.flush({});
  });

  it('does not attach Authorization header when no token', () => {
    auth.getAccessToken = () => null;
    http.get('/test').subscribe();
    const req = mock.expectOne('/test');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });
});
