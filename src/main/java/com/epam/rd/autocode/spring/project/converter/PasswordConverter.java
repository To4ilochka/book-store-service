package com.epam.rd.autocode.spring.project.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordConverter implements Converter<String, String> {
    private final PasswordEncoder passwordEncoder;

    @Override
    public String convert(MappingContext<String, String> mappingContext) {
        String rawPassword = mappingContext.getSource();

        if (rawPassword == null) {
            log.debug("Password conversion skipped: input raw password is null");
            return null;
        }

        // SECURITY NOTE: Никогда не логуем значение rawPassword!
        log.debug("Encoding password during DTO->Entity mapping");

        return passwordEncoder.encode(rawPassword);
    }
}