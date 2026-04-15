package edu.tcu.cs.projectpulse.activeweek.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

/**
 * The admin supplies the set of week-start dates that should be marked INACTIVE.
 * All other generated weeks for the section remain active.
 */
public record ActiveWeekSetupRequest(
        @NotNull Set<LocalDate> inactiveWeekStarts
) {}
