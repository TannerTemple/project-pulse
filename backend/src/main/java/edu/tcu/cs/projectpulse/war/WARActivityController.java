package edu.tcu.cs.projectpulse.war;

import edu.tcu.cs.projectpulse.war.dto.WARActivityRequest;
import edu.tcu.cs.projectpulse.war.dto.WARActivityResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/war-activities")
@RequiredArgsConstructor
public class WARActivityController {

    private final WARActivityService warService;

    /** GET /api/war-activities?weekId= — list my activities for a week (UC-27) */
    @GetMapping
    public List<WARActivityResponse> findMyActivities(@RequestParam Long weekId) {
        return warService.findMyActivities(weekId);
    }

    /** POST /api/war-activities — add a new activity (UC-27) */
    @PostMapping
    public ResponseEntity<WARActivityResponse> create(@Valid @RequestBody WARActivityRequest request) {
        WARActivityResponse created = warService.create(request);
        return ResponseEntity.created(URI.create("/api/war-activities/" + created.id()))
                .body(created);
    }

    /** PUT /api/war-activities/{id} — edit an activity (UC-27) */
    @PutMapping("/{id}")
    public WARActivityResponse update(@PathVariable Long id,
                                      @Valid @RequestBody WARActivityRequest request) {
        return warService.update(id, request);
    }

    /** DELETE /api/war-activities/{id} — remove an activity (UC-27) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        warService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
