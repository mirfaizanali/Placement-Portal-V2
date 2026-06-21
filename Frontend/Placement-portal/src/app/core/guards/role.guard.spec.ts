import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';

describe('roleGuard', () => {
  let routerSpy: jasmine.SpyObj<Router>;
  let auth: { userRole: () => string | null };

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    auth = { userRole: () => null };
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useValue: auth }
      ]
    });
  });

  function route(role?: string): ActivatedRouteSnapshot {
    return { data: role ? { requiredRole: role } : {} } as unknown as ActivatedRouteSnapshot;
  }

  it('returns true when user role matches required role', () => {
    auth.userRole = () => 'STUDENT';
    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route('STUDENT'), {} as RouterStateSnapshot));
    expect(result).toBeTrue();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('returns false and redirects when role does not match', () => {
    auth.userRole = () => 'STUDENT';
    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route('ADMIN'), {} as RouterStateSnapshot));
    expect(result).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/auth/login']);
  });

  it('returns true when no role is required', () => {
    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route(), {} as RouterStateSnapshot));
    expect(result).toBeTrue();
  });
});
