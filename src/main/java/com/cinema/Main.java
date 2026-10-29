package com.cinema;

import com.cinema.model.Screening;
import com.cinema.repository.CsvDataLoader;
import com.cinema.service.BookingService;
import com.cinema.ui.ConsoleMenu;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            CsvDataLoader loader = new CsvDataLoader();
            List<Screening> screenings = loader.loadScreenings("screenings.csv");
            BookingService service = new BookingService(screenings);
            try (Scanner input = new Scanner(System.in)) {
                ConsoleMenu menu = new ConsoleMenu(service, input, System.out);
                menu.run();
            }
        } catch (IOException e) {
            System.err.println("Cannot load screenings: " + e.getMessage());
        }
    }
}
