package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class StudentServiceTest {

    private static final Long ID = 1L;
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final String CLASSE = "L3 Info";

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    // ---------- findById ----------

    @Test
    public void test_findById_existing_student_returns_student() {
        // GIVEN
        Student student = Student.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .classe(CLASSE)
                .build();
        when(studentRepository.findById(ID)).thenReturn(Optional.of(student));

        // WHEN
        Student result = studentService.findById(ID);

        // THEN
        assertThat(result).isEqualTo(student);
    }

    @Test
    public void test_findById_unknown_student_throws_EntityNotFoundException() {
        // GIVEN
        when(studentRepository.findById(ID)).thenReturn(Optional.empty());

        // THEN
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> studentService.findById(ID));
    }

    // ---------- findAll ----------

    @Test
    public void test_findAll_returns_list_of_students() {
        // GIVEN
        Student student1 = Student.builder().id(1L).firstName(FIRST_NAME).lastName(LAST_NAME)
                .email(EMAIL).classe(CLASSE).build();
        Student student2 = Student.builder().id(2L).firstName("Jane").lastName("Smith")
                .email("jane.smith@example.com").classe(CLASSE).build();
        when(studentRepository.findAll()).thenReturn(List.of(student1, student2));

        // WHEN
        List<Student> result = studentService.findAll();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(student1, student2);
    }

    @Test
    public void test_findAll_empty_returns_empty_list() {
        // GIVEN
        when(studentRepository.findAll()).thenReturn(List.of());

        // WHEN
        List<Student> result = studentService.findAll();

        // THEN
        assertThat(result).isEmpty();
    }

    // ---------- create ----------

    @Test
    public void test_create_null_student_throws_IllegalArgumentException() {
        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> studentService.create(null));
    }

    @Test
    public void test_create_email_already_exists_throws_IllegalArgumentException() {
        // GIVEN
        Student student = Student.builder()
                .firstName(FIRST_NAME).lastName(LAST_NAME).email(EMAIL).classe(CLASSE)
                .build();
        when(studentRepository.existsByEmail(EMAIL)).thenReturn(true);

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> studentService.create(student));
    }

    @Test
    public void test_create_student_saves_and_ignores_client_provided_id() {
        // GIVEN
        // Un id est volontairement fourni par le "client" pour vérifier qu'il est neutralisé.
        Student student = Student.builder()
                .id(999L)
                .firstName(FIRST_NAME).lastName(LAST_NAME).email(EMAIL).classe(CLASSE)
                .build();
        when(studentRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(studentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Student result = studentService.create(student);

        // THEN
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentCaptor.capture());
        assertThat(studentCaptor.getValue().getId()).isNull();
        assertThat(result.getEmail()).isEqualTo(EMAIL);
    }

    // ---------- update ----------

    @Test
    public void test_update_unknown_student_throws_EntityNotFoundException() {
        // GIVEN
        when(studentRepository.findById(ID)).thenReturn(Optional.empty());
        Student data = Student.builder()
                .firstName(FIRST_NAME).lastName(LAST_NAME).email(EMAIL).classe(CLASSE)
                .build();

        // THEN
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> studentService.update(ID, data));
    }

    @Test
    public void test_update_student_updates_existing_fields() {
        // GIVEN
        Student existing = Student.builder()
                .id(ID).firstName("Old").lastName("Name").email("old@example.com").classe("L1")
                .build();
        Student data = Student.builder()
                .firstName(FIRST_NAME).lastName(LAST_NAME).email(EMAIL).classe(CLASSE)
                .build();
        when(studentRepository.findById(ID)).thenReturn(Optional.of(existing));
        when(studentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Student result = studentService.update(ID, data);

        // THEN
        assertThat(result.getId()).isEqualTo(ID); // l'id d'origine est conservé
        assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(result.getLastName()).isEqualTo(LAST_NAME);
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getClasse()).isEqualTo(CLASSE);
    }

    // ---------- delete ----------

    @Test
    public void test_delete_unknown_student_throws_EntityNotFoundException() {
        // GIVEN
        when(studentRepository.findById(ID)).thenReturn(Optional.empty());

        // THEN
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> studentService.delete(ID));
        verify(studentRepository, never()).delete(any());
    }

    @Test
    public void test_delete_existing_student_calls_repository_delete() {
        // GIVEN
        Student existing = Student.builder()
                .id(ID).firstName(FIRST_NAME).lastName(LAST_NAME).email(EMAIL).classe(CLASSE)
                .build();
        when(studentRepository.findById(ID)).thenReturn(Optional.of(existing));

        // WHEN
        studentService.delete(ID);

        // THEN
        verify(studentRepository).delete(existing);
    }
}
