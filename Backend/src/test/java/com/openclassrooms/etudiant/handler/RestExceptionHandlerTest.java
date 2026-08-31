package com.openclassrooms.etudiant.handler;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires directs sur RestExceptionHandler : chaque méthode est
 * appelée telle quelle, sans passer par une vraie requête HTTP (MockMvc),
 * pour vérifier isolément le mapping exception -> code HTTP.
 */
public class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    private WebRequest buildWebRequest() {
        return new ServletWebRequest(new MockHttpServletRequest());
    }

    // ---------- IllegalArgumentException / IllegalStateException -> 400 ----------

    @Test
    public void handleConflict_illegalArgumentException_returnsBadRequest() {
        // GIVEN
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        // WHEN
        ResponseEntity<Object> response = handler.handleConflict(exception, buildWebRequest());

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ErrorDetails.class);
        assertThat(((ErrorDetails) response.getBody()).getMessage()).isEqualTo("Invalid input");
    }

    @Test
    public void handleConflict_illegalStateException_returnsBadRequest() {
        // GIVEN
        IllegalStateException exception = new IllegalStateException("Invalid state");

        // WHEN
        ResponseEntity<Object> response = handler.handleConflict(exception, buildWebRequest());

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ---------- BadCredentialsException -> 401 ----------

    @Test
    public void handleBadCredentialsException_returnsUnauthorized() {
        // GIVEN
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // WHEN
        ResponseEntity<Object> response = handler.handleBadCredentialsException(exception, buildWebRequest());

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isInstanceOf(ErrorDetails.class);
    }

    // ---------- EntityNotFoundException -> 404 ----------

    @Test
    public void handleNotFoundException_returnsNotFound() {
        // GIVEN
        EntityNotFoundException exception = new EntityNotFoundException("Student not found with id 1");

        // WHEN
        ResponseEntity<Object> response = handler.handleNotFoundException(exception, buildWebRequest());

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(((ErrorDetails) response.getBody()).getMessage()).isEqualTo("Student not found with id 1");
    }

    // ---------- AccessDeniedException -> 403 ----------

    @Test
    public void handleForbiddenException_returnsForbidden() {
        // GIVEN
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // WHEN
        ResponseEntity<Object> response = handler.handleForbiddenException(exception, buildWebRequest());

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isInstanceOf(ErrorDetails.class);
    }

    // ---------- Exception générique -> 500 ----------

    @Test
    public void handleException_genericRuntimeException_returnsInternalServerError() {
        // GIVEN
        RuntimeException exception = new RuntimeException("Unexpected error");

        // WHEN
        ResponseEntity<Object> response = handler.handleException(exception, buildWebRequest());

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Internal Server error");
    }
}
