import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { StudentService } from './student.service';
import { Student } from '../models/Student';

describe('StudentService', () => {
  let service: StudentService;
  let httpMock: HttpTestingController;

  const student: Student = {
    firstName: 'John',
    lastName: 'Doe',
    email: 'john.doe@example.com',
    classe: 'L3 Info'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [StudentService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(StudentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should GET all students', () => {
    // WHEN
    service.getAll().subscribe(result => {
      expect(result).toEqual([student]);
    });

    // THEN
    const req = httpMock.expectOne('/api/students');
    expect(req.request.method).toBe('GET');
    req.flush([student]);
  });

  it('should GET a student by id', () => {
    // WHEN
    service.getById(1).subscribe(result => {
      expect(result).toEqual(student);
    });

    // THEN
    const req = httpMock.expectOne('/api/students/1');
    expect(req.request.method).toBe('GET');
    req.flush(student);
  });

  it('should POST a new student', () => {
    // WHEN
    service.create(student).subscribe(result => {
      expect(result).toEqual({ ...student, id: 1 });
    });

    // THEN
    const req = httpMock.expectOne('/api/students');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(student);
    req.flush({ ...student, id: 1 }, { status: 201, statusText: 'Created' });
  });

  it('should PUT an updated student', () => {
    // GIVEN
    const updated: Student = { ...student, firstName: 'Jane' };

    // WHEN
    service.update(1, updated).subscribe(result => {
      expect(result).toEqual(updated);
    });

    // THEN
    const req = httpMock.expectOne('/api/students/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updated);
    req.flush(updated);
  });

  it('should DELETE a student', () => {
    // WHEN
    service.delete(1).subscribe();

    // THEN
    const req = httpMock.expectOne('/api/students/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
  });
});