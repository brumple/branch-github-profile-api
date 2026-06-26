package com.branchinterview.githubprofile.controller;

public record ErrorResponse(String code, String message, int status, String requestId) {
}
