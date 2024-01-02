package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.exceptions.InvalidUrlException;
import com.everepl.evereplspringboot.service.UrlInfoService;
import com.everepl.evereplspringboot.dto.UrlInfoRequest;
import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> processUrl(@RequestBody UrlInfoRequest urlInfoRequest) {
        try {
            UrlInfoResponse urlInfoResponse = urlInfoService.processUrl(urlInfoRequest.url());
            return ResponseEntity.ok(urlInfoResponse);
        } catch (InvalidUrlException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUrlInfo(@PathVariable Long id) {
        try {
            UrlInfo urlInfo = urlInfoService.getUrlInfoById(id);
            return ResponseEntity.ok(urlInfo);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database access error: " + e.getMessage());
        }
    }


}
