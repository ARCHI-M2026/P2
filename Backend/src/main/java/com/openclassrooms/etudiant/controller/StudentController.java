package com.openclassrooms.etudiant.controller;

import com.openclassrooms.etudiant.dto.StudentDTO;
import com.openclassrooms.etudiant.mapper.StudentMapper;
import com.openclassrooms.etudiant.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final StudentMapper studentMapper;

    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAll() {
        return ResponseEntity.ok(studentMapper.toDtoList(studentService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentMapper.toDto(studentService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<StudentDTO> create(@Valid @RequestBody StudentDTO dto) {
        StudentDTO created = studentMapper.toDto(studentService.create(studentMapper.toEntity(dto)));
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> update(@PathVariable Long id, @Valid @RequestBody StudentDTO dto) {
        StudentDTO updated = studentMapper.toDto(studentService.update(id, studentMapper.toEntity(dto)));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
