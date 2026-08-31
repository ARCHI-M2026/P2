package com.openclassrooms.etudiant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.etudiant.dto.LoginRequestDTO;
import com.openclassrooms.etudiant.dto.RegisterDTO;
import com.openclassrooms.etudiant.dto.StudentDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import com.openclassrooms.etudiant.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class StudentControllerTest {

    private static final String URL = "/api/students";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final String CLASSE = "L3 Info";

    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.4");

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    private String jwtToken;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        // On crée un utilisateur puis on se connecte réellement via /api/login
        // pour obtenir un vrai token JWT, utilisé ensuite dans chaque test.
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName(FIRST_NAME);
        registerDTO.setLastName(LAST_NAME);
        registerDTO.setLogin(LOGIN);
        registerDTO.setPassword(PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .content(objectMapper.writeValueAsString(registerDTO))
                .contentType(MediaType.APPLICATION_JSON));

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setLogin(LOGIN);
        loginRequestDTO.setPassword(PASSWORD);

        String response = mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .content(objectMapper.writeValueAsString(loginRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // La réponse de /api/login est le token brut, sans guillemets JSON à retirer
        // (ResponseEntity.ok(jwtToken) sérialise directement la String).
        jwtToken = response.replace("\"", "");
    }

    @AfterEach
    public void afterEach() {
        studentRepository.deleteAll();
        userRepository.deleteAll();
    }

    private StudentDTO buildStudentDTO() {
        StudentDTO dto = new StudentDTO();
        dto.setFirstName(FIRST_NAME);
        dto.setLastName(LAST_NAME);
        dto.setEmail(EMAIL);
        dto.setClasse(CLASSE);
        return dto;
    }

    // ---------- Sécurité : accès sans token ----------

    @Test
    public void getAll_withoutToken_returnsUnauthorized() throws Exception {
        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    // ---------- POST /api/students ----------

    @Test
    public void create_validStudent_returnsCreated() throws Exception {
        // GIVEN
        StudentDTO dto = buildStudentDTO();

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.post(URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void create_invalidStudent_returnsBadRequest() throws Exception {
        // GIVEN
        StudentDTO dto = new StudentDTO(); // tous les champs obligatoires sont null

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.post(URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ---------- GET /api/students/{id} ----------

    @Test
    public void getById_existingStudent_returnsOk() throws Exception {
        // GIVEN
        Student student = studentRepository.save(Student.builder()
                .firstName(FIRST_NAME).lastName(LAST_NAME).email(EMAIL).classe(CLASSE)
                .build());

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/" + student.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(EMAIL));
    }

    @Test
    public void getById_unknownStudent_returnsNotFound() throws Exception {
        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/999999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    // ---------- GET /api/students ----------

    @Test
    public void getAll_withStudents_returnsOkWithList() throws Exception {
        // GIVEN
        studentRepository.save(Student.builder()
                .firstName(FIRST_NAME).lastName(LAST_NAME).email(EMAIL).classe(CLASSE)
                .build());

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                        .header("Authorization", "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1));
    }

    @Test
    public void getAll_emptyDatabase_returnsOkWithEmptyList() throws Exception {
        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                        .header("Authorization", "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
    }

    // ---------- PUT /api/students/{id} ----------

    @Test
    public void update_validStudent_returnsOk() throws Exception {
        // GIVEN
        Student student = studentRepository.save(Student.builder()
                .firstName("Old").lastName("Name").email("old@example.com").classe("L1")
                .build());
        StudentDTO dto = buildStudentDTO();

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/" + student.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(EMAIL));
    }

    @Test
    public void update_invalidStudent_returnsBadRequest() throws Exception {
        // GIVEN
        Student student = studentRepository.save(Student.builder()
                .firstName(FIRST_NAME).lastName(LAST_NAME).email(EMAIL).classe(CLASSE)
                .build());
        StudentDTO dto = new StudentDTO(); // champs obligatoires null

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/" + student.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ---------- DELETE /api/students/{id} ----------

    @Test
    public void delete_existingStudent_returnsNoContent() throws Exception {
        // GIVEN
        Student student = studentRepository.save(Student.builder()
                .firstName(FIRST_NAME).lastName(LAST_NAME).email(EMAIL).classe(CLASSE)
                .build());

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/" + student.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void delete_unknownStudent_returnsNotFound() throws Exception {
        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/999999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}