package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.exceptions.InvalidUrlException;
import com.everepl.evereplspringboot.service.UrlInfoService;
import com.everepl.evereplspringboot.dto.UrlInfoRequest;
import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/url")
public class UrlInfoController {

    private final UrlInfoService urlInfoService;

    @Autowired
    public UrlInfoController(UrlInfoService urlInfoService) {
        this.urlInfoService = urlInfoService;
    }

    @PostMapping
    public ResponseEntity<?> processUrl(@Validated @RequestBody UrlInfoRequest urlInfoRequest) {
            UrlInfoResponse urlInfoResponse = urlInfoService.processUrl(urlInfoRequest.url());
            return ResponseEntity.ok(urlInfoResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUrlInfo(@PathVariable Long id) {
            UrlInfoResponse urlInfoResponse = urlInfoService.getUrlInfoById(id);
            return ResponseEntity.ok(urlInfoResponse);
    }

    @GetMapping
    public ResponseEntity<?> getUrlInfos(
            @RequestParam(required = false) List<String> filterStrings,
            Pageable pageable) {
            Page<UrlInfoResponse> urlInfos = urlInfoService.getUrlInfos(filterStrings, pageable);
            return ResponseEntity.ok(urlInfos);
    }

}
