import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { Profile } from './profile';
import { environment } from '../../../../environments/environment';

/**
 * Locks B1 — the student profile form must hydrate preferredLocations and
 * preferredJobTypes from the loaded profile, and must include them in the
 * PUT payload when the user saves.
 */
describe('Student Profile', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Profile],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideAnimationsAsync()
      ]
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  function loadComponent() {
    const fixture = TestBed.createComponent(Profile);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    const req = httpMock.expectOne(`${environment.apiUrl}/api/students/me`);
    req.flush({
      success: true, message: 'ok',
      data: {
        id: 'sp-1', userId: 'u-1', fullName: 'Ada', email: 'ada@ex.com',
        rollNumber: '21CS1', department: 'CS', batchYear: 2025, cgpa: 8.5,
        phone: '+91', linkedinUrl: '', githubUrl: '', bio: '',
        preferredLocations: 'Bangalore, Pune', preferredJobTypes: 'FULL_TIME',
        isPlaced: false, placementPackage: null, placedCompany: null,
        facultyMentorId: null, skills: [], createdAt: '2026-01-01'
      }
    });

    return { fixture, component };
  }

  it('hydrates preferredLocations and preferredJobTypes from the loaded profile (B1)', () => {
    const { component } = loadComponent();
    expect(component.editForm().preferredLocations).toBe('Bangalore, Pune');
    expect(component.editForm().preferredJobTypes).toBe('FULL_TIME');
  });

  it('save() includes preferredLocations and preferredJobTypes in the PUT payload (B1)', () => {
    const { component } = loadComponent();

    component.updateField('preferredLocations', 'Mumbai, Remote');
    component.updateField('preferredJobTypes', 'INTERNSHIP');
    component.save();

    const put = httpMock.expectOne(`${environment.apiUrl}/api/students/me`);
    expect(put.request.method).toBe('PUT');
    expect(put.request.body.preferredLocations).toBe('Mumbai, Remote');
    expect(put.request.body.preferredJobTypes).toBe('INTERNSHIP');
    put.flush({ success: true, message: 'ok', data: {} });
  });
});
