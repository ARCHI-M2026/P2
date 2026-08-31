import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MaterialModule } from '../../shared/material.module';
import { StudentService } from '../../core/service/student.service';
import { Student } from '../../core/models/Student';

@Component({
  selector: 'app-students',
  standalone: true,
  imports: [CommonModule, MaterialModule],
  templateUrl: './students.component.html',
  styleUrl: './students.component.css'
})
export class StudentsComponent implements OnInit {
  private studentService = inject(StudentService);
  private formBuilder = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  students: Student[] = [];
  form: FormGroup = new FormGroup({});
  editingId: number | null = null;

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      classe: ['', Validators.required]
    });
    this.load();
  }

  load(): void {
    this.studentService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(data => this.students = data);
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    const student: Student = this.form.value;
    const request$ = this.editingId
      ? this.studentService.update(this.editingId, student)
      : this.studentService.create(student);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.resetForm();
      this.load();
    });
  }

  edit(student: Student): void {
    this.editingId = student.id ?? null;
    this.form.patchValue(student);
  }

  remove(id?: number): void {
    if (id == null) {
      return;
    }
    this.studentService.delete(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.load());
  }

  resetForm(): void {
    this.editingId = null;
    this.form.reset();
  }
}
