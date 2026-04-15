package edu.tcu.cs.projectpulse.section;

import edu.tcu.cs.projectpulse.section.dto.SectionRequest;
import edu.tcu.cs.projectpulse.section.dto.SectionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SectionResponse> findAll(@RequestParam(required = false) String name) {
        return sectionService.findAll(name);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SectionResponse findById(@PathVariable Long id) {
        return sectionService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionResponse> create(@Valid @RequestBody SectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SectionResponse update(@PathVariable Long id, @Valid @RequestBody SectionRequest request) {
        return sectionService.update(id, request);
    }
}
