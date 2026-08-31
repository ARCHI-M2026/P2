import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { LoginRequest } from '../models/LoginRequest';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should login via API and store the token', () => {
    // GIVEN
    const credentials: LoginRequest = { login: 'jdoe', password: 'pass' };

    // WHEN
    service.login(credentials).subscribe();

    // THEN
    const req = httpMock.expectOne('/api/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(credentials);
    req.flush('my-token');

    expect(service.getToken()).toBe('my-token');
  });

  it('should persist token in localStorage', () => {
    // WHEN
    service.setToken('my-token');

    // THEN
    expect(service.getToken()).toBe('my-token');
  });

  it('should detect unauthenticated state when no token is stored', () => {
    // THEN
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should detect authenticated state when a token is stored', () => {
    // GIVEN
    service.setToken('my-token');

    // THEN
    expect(service.isAuthenticated()).toBe(true);
  });

  it('should clear the token on logout', () => {
    // GIVEN
    service.setToken('my-token');

    // WHEN
    service.logout();

    // THEN
    expect(service.getToken()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });
});