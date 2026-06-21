import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { HasRoleDirective } from './has-role.directive';
import { AuthService } from '../../core/services/auth.service';

@Component({
  standalone: true,
  imports: [HasRoleDirective],
  template: `<span *appHasRole="role">visible</span>`
})
class HostComponent {
  role: string | string[] = 'ADMIN';
}

describe('HasRoleDirective', () => {
  let currentRole: () => string | null;

  beforeEach(() => {
    currentRole = () => 'ADMIN';
    TestBed.configureTestingModule({
      imports: [HostComponent],
      providers: [{ provide: AuthService, useValue: { userRole: () => currentRole() } }]
    });
  });

  it('renders the element when user role matches', () => {
    currentRole = () => 'ADMIN';
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('visible');
  });

  it('does not render when user role does not match', () => {
    currentRole = () => 'STUDENT';
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('visible');
  });

  it('renders when user role is in array of allowed roles', () => {
    currentRole = () => 'EMPLOYER';
    const fixture = TestBed.createComponent(HostComponent);
    fixture.componentInstance.role = ['ADMIN', 'EMPLOYER'];
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('visible');
  });
});
