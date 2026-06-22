package com.bankingassistant.mcp.server;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private static final String LOG_FILE = "mcp/bank-audit.log";

    public List<String> search(String keyword) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(LOG_FILE).getInputStream()))) {
            return reader.lines()
                    .filter(line -> !line.isBlank())
                    .filter(line -> line.toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }
}
