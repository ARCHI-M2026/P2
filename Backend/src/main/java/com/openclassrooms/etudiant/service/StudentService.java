package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Logique métier du CRUD des étudiants.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Student findById(Long id) {
        Assert.notNull(id, "Id must not be null");
        return studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id " + id));
    }

    public Student create(Student student) {
        Assert.notNull(student, "Student must not be null");
        student.setId(null); // ID à null pour éviter conflit et tentative
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("Student with email " + student.getEmail() + " already exists");
        }
        log.info("Student created {}", student.getEmail());
        return studentRepository.save(student);
    }

    public Student update(Long id, Student data) {
        Student existing = findById(id);
        existing.setFirstName(data.getFirstName());
        existing.setLastName(data.getLastName());
        existing.setEmail(data.getEmail());
        existing.setClasse(data.getClasse());
        log.info("Student updated {}", id);
        return studentRepository.save(existing);
    }

    public void delete(Long id) {
        Student existing = findById(id);
        studentRepository.delete(existing);
        log.info("Student deleted {}", id);
    }
}
