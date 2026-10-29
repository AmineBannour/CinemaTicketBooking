package com.cinema.repository;

import com.cinema.model.Screening;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CsvDataLoader {
    public List<Screening> loadScreenings(String resourceName) throws IOException {
        InputStream stream = CsvDataLoader.class.getResourceAsStream("/" + resourceName);
        if (stream == null) {
            throw new IOException("Missing file: " + resourceName);
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return readScreenings(reader);
        }
    }

    public List<Screening> readScreenings(Reader source) throws IOException {
        BufferedReader reader = new BufferedReader(source);
        String header = reader.readLine();
        if (header == null || !header.equals("id,movie,priceCents,seats")) {
            throw new IOException("Expected header: id,movie,priceCents,seats");
        }
        List<Screening> screenings = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        String line;
        int lineNumber = 1;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            try {
                String[] columns = line.split(",", -1);
                if (columns.length != 4 || line.contains("\"")) {
                    throw new IllegalArgumentException("Use four simple columns without quoted fields.");
                }
                int id = Integer.parseInt(columns[0].trim());
                String title = columns[1].trim();
                int price = Integer.parseInt(columns[2].trim());
                int seats = Integer.parseInt(columns[3].trim());
                if (!ids.add(id)) {
                    throw new IllegalArgumentException("Duplicate screening ID: " + id);
                }
                screenings.add(new Screening(id, title, price, seats));
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid data on line " + lineNumber + ": " + e.getMessage(), e);
            }
        }
        if (screenings.isEmpty()) {
            throw new IOException("The file must contain at least one screening.");
        }
        return List.copyOf(screenings);
    }
}
