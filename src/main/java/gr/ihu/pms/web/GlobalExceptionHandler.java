package gr.ihu.pms.web;

import gr.ihu.pms.common.NotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

/** Κοινός χειρισμός σφαλμάτων για όλους τους controllers. */
@ControllerAdvice
public class GlobalExceptionHandler {

    /** NotFoundException → HTTP 404 (σελίδα error.html). */
    @ExceptionHandler(NotFoundException.class)
    public void notFound(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value());
    }
}
