import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { StudentsComponent } from './students.component';
import { StudentService } from '../../core/service/student.service';
import { Student } from '../../core/models/Student';

describe('StudentsComponent', () => {
  let component: StudentsComponent;
  let fixture: ComponentFixture<StudentsComponent>;
  let studentServiceMock: {
    getAll: jest.Mock;
    create: jest.Mock;
    update: jest.Mock;
    delete: jest.Mock;
  };

  const student1: Student = { id: 1, firstName: 'John', lastName: 'Doe', email: 'john@test.com', classe: 'L3' };
  const student2: Student = { id: 2, firstName: 'Jane', lastName: 'Smith', email: 'jane@test.com', classe: 'L2' };

  beforeEach(async () => {
    studentServiceMock = {
      getAll: jest.fn().mockReturnValue(of([student1, student2])),
      create: jest.fn(),
      update: jest.fn(),
      delete: jest.fn()
    };

    await TestBed.configureTestingModule({
      imports: [StudentsComponent, ReactiveFormsModule],
      providers: [
        { provide: StudentService, useValue: studentServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StudentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should load the student list on init', () => {
    // THEN
    expect(studentServiceMock.getAll).toHaveBeenCalledTimes(1);
    expect(component.students).toEqual([student1, student2]);
  });

  it('should create a student successfully on submit when not editing', () => {
    // GIVEN
    studentServiceMock.create.mockReturnValue(of({ ...student1 }));
    component.editingId = null;
    component.form.setValue({ firstName: 'John', lastName: 'Doe', email: 'john@test.com', classe: 'L3' });

    // WHEN
    component.submit();

    // THEN
    expect(studentServiceMock.create).toHaveBeenCalledWith({
      firstName: 'John', lastName: 'Doe', email: 'john@test.com', classe: 'L3'
    });
    expect(studentServiceMock.update).not.toHaveBeenCalled();
  });

  it('should update a student successfully on submit when editing', () => {
    // GIVEN
    studentServiceMock.update.mockReturnValue(of({ ...student1 }));
    component.editingId = 1;
    component.form.setValue({ firstName: 'John', lastName: 'Doe', email: 'john@test.com', classe: 'L3' });

    // WHEN
    component.submit();

    // THEN
    expect(studentServiceMock.update).toHaveBeenCalledWith(1, {
      firstName: 'John', lastName: 'Doe', email: 'john@test.com', classe: 'L3'
    });
    expect(studentServiceMock.create).not.toHaveBeenCalled();
  });

  it('should reset the form and reload the list after a successful submit', () => {
    // GIVEN
    studentServiceMock.create.mockReturnValue(of({ ...student1 }));
    component.editingId = null;
    component.form.setValue({ firstName: 'John', lastName: 'Doe', email: 'john@test.com', classe: 'L3' });

    // WHEN
    component.submit();

    // THEN
    expect(component.editingId).toBeNull();
    expect(component.form.value.firstName).toBeNull();
    expect(studentServiceMock.getAll).toHaveBeenCalledTimes(2); // 1 au ngOnInit + 1 après submit
  });

  it('should not call create or update when the form is invalid', () => {
    // GIVEN
    component.form.setValue({ firstName: '', lastName: '', email: '', classe: '' });

    // WHEN
    component.submit();

    // THEN
    expect(studentServiceMock.create).not.toHaveBeenCalled();
    expect(studentServiceMock.update).not.toHaveBeenCalled();
  });

  it('should not call create when email is invalid', () => {
    // GIVEN
    component.form.setValue({ firstName: 'John', lastName: 'Doe', email: 'not-an-email', classe: 'L3' });

    // WHEN
    component.submit();

    // THEN
    expect(studentServiceMock.create).not.toHaveBeenCalled();
  });

  it('should populate the form and set editingId on edit()', () => {
    // WHEN
    component.edit(student1);

    // THEN
    expect(component.editingId).toBe(1);
    expect(component.form.value.firstName).toBe('John');
    expect(component.form.value.email).toBe('john@test.com');
  });

  it('should set editingId to null when editing a student without id', () => {
    // GIVEN
    const studentWithoutId: Student = { firstName: 'Anon', lastName: 'Ymous', email: 'a@test.com', classe: 'L1' };

    // WHEN
    component.edit(studentWithoutId);

    // THEN
    expect(component.editingId).toBeNull();
  });

  it('should delete a student and reload the list', () => {
    // GIVEN
    studentServiceMock.delete.mockReturnValue(of(undefined));

    // WHEN
    component.remove(1);

    // THEN
    expect(studentServiceMock.delete).toHaveBeenCalledWith(1);
    expect(studentServiceMock.getAll).toHaveBeenCalledTimes(2); // 1 au ngOnInit + 1 après suppression
  });

  it('should not call delete when id is undefined', () => {
    // WHEN
    component.remove(undefined);

    // THEN
    expect(studentServiceMock.delete).not.toHaveBeenCalled();
  });

  it('should reset the form and clear editingId on resetForm()', () => {
    // GIVEN
    component.editingId = 1;
    component.form.setValue({ firstName: 'John', lastName: 'Doe', email: 'john@test.com', classe: 'L3' });

    // WHEN
    component.resetForm();

    // THEN
    expect(component.editingId).toBeNull();
    expect(component.form.value.firstName).toBeNull();
  });
});