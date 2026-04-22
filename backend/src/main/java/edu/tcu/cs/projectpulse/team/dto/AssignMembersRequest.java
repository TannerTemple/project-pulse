package edu.tcu.cs.projectpulse.team.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AssignMembersRequest(@NotEmpty List<Long> userIds) {}
