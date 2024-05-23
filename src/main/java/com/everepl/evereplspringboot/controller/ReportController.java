package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.dto.ReportRequest;
import com.everepl.evereplspringboot.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<?> report(@RequestBody ReportRequest reportRequest) {
        reportService.handleReport(reportRequest);
        return ResponseEntity.ok("신고가 접수되었습니다.");
    }
}