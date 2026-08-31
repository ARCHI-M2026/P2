import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { UserService } from './user.service';
import { Register } from '../models/Register';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  const registerUser: Register = {
    firstName: 'John',
    lastName: 'Doe',
    login: 'jdoe',
    password: 'pass'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UserService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should register a user via the API', () => {
    // WHEN
    service.register(registerUser).subscribe(result => {
      expect(result).toEqual(registerUser);
    });

    // THEN
    const req = httpMock.expectOne('/api/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(registerUser);
    req.flush(registerUser, { status: 201, statusText: 'Created' });
  });

  it('should propagate a 400 error when registration fails', () => {
    // GIVEN
    const errorPayload = { message: 'User already exists' };

    // WHEN
    service.register(registerUser).subscribe({
      next: () => fail('expected an error, not a success'),
      error: (err) => {
        expect(err.status).toBe(400);
        expect(err.error).toEqual(errorPayload);
      }
    });

    // THEN
    const req = httpMock.expectOne('/api/register');
    req.flush(errorPayload, { status: 400, statusText: 'Bad Request' });
  });
});