package com.everepl.evereplspringboot.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendErrorEmail(String subject, String text) {
        System.out.println(subject + ": " + text);
    }
}