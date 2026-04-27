package edu.tcu.cs.projectpulse.rubric;

import edu.tcu.cs.projectpulse.rubric.dto.RubricRequest;
import edu.tcu.cs.projectpulse.rubric.dto.RubricResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rubrics")
@RequiredArgsConstructor
public class RubricController {

    private final RubricService rubricService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<RubricResponse> findAll() {
        return rubricService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RubricResponse findById(@PathVariable Long id) {
        return rubricService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RubricResponse> create(@Valid @RequestBody RubricRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rubricService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RubricResponse update(@PathVariable Long id, @Valid @RequestBody RubricRequest request) {
        return rubricService.update(id, request);
    }
}
