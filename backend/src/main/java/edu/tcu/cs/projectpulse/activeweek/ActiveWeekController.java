package edu.tcu.cs.projectpulse.activeweek;

import edu.tcu.cs.projectpulse.activeweek.dto.ActiveWeekSetupRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections/{sectionId}/weeks")
@RequiredArgsConstructor
public class ActiveWeekController {

    private final ActiveWeekService activeWeekService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ActiveWeek> findAll(@PathVariable Long sectionId) {
        return activeWeekService.findBySectionId(sectionId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ActiveWeek> setup(@PathVariable Long sectionId,
                                  @Valid @RequestBody ActiveWeekSetupRequest request) {
        return activeWeekService.setupWeeks(sectionId, request);
    }
}
