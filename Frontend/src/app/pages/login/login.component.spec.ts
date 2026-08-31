import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../core/service/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceMock: { login: jest.Mock };
  let routerMock: { navigate: jest.Mock };

  beforeEach(async () => {
    authServiceMock = { login: jest.fn() };
    routerMock = { navigate: jest.fn() };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should call authService.login and navigate on valid submit', () => {
    // GIVEN
    authServiceMock.login.mockReturnValue(of('fake-token'));
    component.loginForm.setValue({ login: 'jdoe', password: 'pass' });

    // WHEN
    component.onSubmit();

    // THEN
    expect(authServiceMock.login).toHaveBeenCalledWith({ login: 'jdoe', password: 'pass' });
    expect(routerMock.navigate).toHaveBeenCalledWith(['/etudiants']);
  });

  it('should display an error message on failed login', () => {
    // GIVEN
    authServiceMock.login.mockReturnValue(throwError(() => new Error('unauthorized')));
    component.loginForm.setValue({ login: 'jdoe', password: 'wrong' });

    // WHEN
    component.onSubmit();

    // THEN
    expect(component.errorMessage).toBe('Identifiants invalides');
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('should reject an empty login form', () => {
    // GIVEN
    component.loginForm.setValue({ login: '', password: '' });

    // WHEN
    component.onSubmit();

    // THEN
    expect(authServiceMock.login).not.toHaveBeenCalled();
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('should reject a form with a missing login field', () => {
    // GIVEN
    component.loginForm.setValue({ login: '', password: 'pass' });

    // WHEN
    component.onSubmit();

    // THEN
    expect(authServiceMock.login).not.toHaveBeenCalled();
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('should reject a form with a missing password field', () => {
    // GIVEN
    component.loginForm.setValue({ login: 'jdoe', password: '' });

    // WHEN
    component.onSubmit();

    // THEN
    expect(authServiceMock.login).not.toHaveBeenCalled();
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('should reset submitted and errorMessage on each submit attempt', () => {
    // GIVEN
    authServiceMock.login.mockReturnValue(of('fake-token'));
    component.errorMessage = 'previous error';
    component.loginForm.setValue({ login: 'jdoe', password: 'pass' });

    // WHEN
    component.onSubmit();

    // THEN
    expect(component.submitted).toBe(true);
    expect(component.errorMessage).toBe('');
  });
});