import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let routerSpy: jasmine.SpyObj<Router>;
  let auth: { isAuthenticated: () => boolean };

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    auth = { isAuthenticated: () => false };

    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useValue: auth }
      ]
    });
  });

  it('returns true when authenticated', () => {
    auth.isAuthenticated = () => true;
    expect(TestBed.runInInjectionContext(() => authGuard(
      {} as ActivatedRouteSnapshot, {} as RouterStateSnapshot))).toBeTrue();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('returns false and redirects to /auth/login when not authenticated', () => {
    auth.isAuthenticated = () => false;
    const result = TestBed.runInInjectionContext(() => authGuard(
      {} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));
    expect(result).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
