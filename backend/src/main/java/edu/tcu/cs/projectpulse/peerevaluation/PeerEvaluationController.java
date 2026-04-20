package edu.tcu.cs.projectpulse.peerevaluation;

import edu.tcu.cs.projectpulse.peerevaluation.dto.PeerEvalReportResponse;
import edu.tcu.cs.projectpulse.peerevaluation.dto.PeerEvaluationRequest;
import edu.tcu.cs.projectpulse.peerevaluation.dto.PeerEvaluationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/peer-evaluations")
@RequiredArgsConstructor
public class PeerEvaluationController {

    private final PeerEvaluationService evalService;

    /** POST /api/peer-evaluations — submit an evaluation (UC-28) */
    @PostMapping
    public ResponseEntity<PeerEvaluationResponse> submit(
            @Valid @RequestBody PeerEvaluationRequest request) {
        PeerEvaluationResponse created = evalService.submit(request);
        return ResponseEntity
                .created(URI.create("/api/peer-evaluations/" + created.id()))
                .body(created);
    }

    /** GET /api/peer-evaluations/me/report?weekId= — own report (UC-29) */
    @GetMapping("/me/report")
    public PeerEvalReportResponse getMyReport(@RequestParam Long weekId) {
        return evalService.getMyReport(weekId);
    }
}
