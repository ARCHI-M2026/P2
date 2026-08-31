import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { RegisterComponent } from './register.component';
import { UserService } from '../../core/service/user.service';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let userServiceMock: { register: jest.Mock };
  let alertSpy: jest.SpyInstance;

  const validForm = {
    firstName: 'John',
    lastName: 'Doe',
    login: 'jdoe',
    password: 'pass'
  };

  beforeEach(async () => {
    userServiceMock = { register: jest.fn() };
    alertSpy = jest.spyOn(window, 'alert').mockImplementation(() => {});

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, ReactiveFormsModule],
      providers: [
        { provide: UserService, useValue: userServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    alertSpy.mockRestore();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should call userService.register with form values on valid submit', () => {
    // GIVEN
    userServiceMock.register.mockReturnValue(of({}));
    component.registerForm.setValue(validForm);

    // WHEN
    component.onSubmit();

    // THEN
    expect(userServiceMock.register).toHaveBeenCalledWith(validForm);
    expect(alertSpy).toHaveBeenCalledWith('SUCCESS!! :-)');
  });

  it('should reject an empty register form', () => {
    // GIVEN
    component.registerForm.setValue({ firstName: '', lastName: '', login: '', password: '' });

    // WHEN
    component.onSubmit();

    // THEN
    expect(userServiceMock.register).not.toHaveBeenCalled();
  });

  it('should reject a form with a missing firstName', () => {
    // GIVEN
    component.registerForm.setValue({ ...validForm, firstName: '' });

    // WHEN
    component.onSubmit();

    // THEN
    expect(userServiceMock.register).not.toHaveBeenCalled();
  });

  it('should reject a form with a missing lastName', () => {
    // GIVEN
    component.registerForm.setValue({ ...validForm, lastName: '' });

    // WHEN
    component.onSubmit();

    // THEN
    expect(userServiceMock.register).not.toHaveBeenCalled();
  });

  it('should reject a form with a missing login', () => {
    // GIVEN
    component.registerForm.setValue({ ...validForm, login: '' });

    // WHEN
    component.onSubmit();

    // THEN
    expect(userServiceMock.register).not.toHaveBeenCalled();
  });

  it('should reject a form with a missing password', () => {
    // GIVEN
    component.registerForm.setValue({ ...validForm, password: '' });

    // WHEN
    component.onSubmit();

    // THEN
    expect(userServiceMock.register).not.toHaveBeenCalled();
  });

  it('should reset the form on onReset', () => {
    // GIVEN
    component.submitted = true;
    component.registerForm.setValue(validForm);

    // WHEN
    component.onReset();

    // THEN
    expect(component.submitted).toBe(false);
    expect(component.registerForm.value).toEqual({
      firstName: null,
      lastName: null,
      login: null,
      password: null
    });
  });
});