package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.service.UrlInfoService;
import com.everepl.evereplspringboot.dto.UrlInfoRequest;
import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/url")
public class UrlInfoController {

    private final UrlInfoService urlInfoService;

    @Autowired
    public UrlInfoController(UrlInfoService urlInfoService) {
        this.urlInfoService = urlInfoService;
    }

    @PostMapping
    public ResponseEntity<UrlInfoResponse> processUrl(@RequestBody UrlInfoRequest urlInfoRequest) {
        UrlInfoResponse urlInfoResponse = urlInfoService.processUrl(urlInfoRequest.url());
        return ResponseEntity.ok(urlInfoResponse);
    }

}
