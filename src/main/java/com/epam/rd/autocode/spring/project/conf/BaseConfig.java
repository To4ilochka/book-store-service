package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.converter.PasswordConverter;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BaseConfig {

    @Bean
    public ModelMapper modelMapper(PasswordConverter passwordConverter) {
        log.debug("Initializing ModelMapper with custom PasswordConverter");

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true);

        modelMapper.typeMap(ClientDTO.class, Client.class)
                .addMappings(m -> m.using(passwordConverter)
                        .map(ClientDTO::getPassword, Client::setPassword));
        modelMapper.typeMap(EmployeeDTO.class, Employee.class)
                .addMappings(m -> m.using(passwordConverter)
                        .map(EmployeeDTO::getPassword, Employee::setPassword));

        return modelMapper;
    }
}