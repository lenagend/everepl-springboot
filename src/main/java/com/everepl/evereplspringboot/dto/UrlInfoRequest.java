package com.everepl.evereplspringboot.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record UrlInfoRequest(
        @URL
        @NotNull
        String url
) {}