package com.cinema.repository;

import com.cinema.model.Screening;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CsvDataLoaderTest {
    private final CsvDataLoader loader = new CsvDataLoader();
    private final String header = "id,movie,priceCents,seats\n";

    @Test
    void loadsBundledScreenings() throws IOException {
        List<Screening> screenings = loader.loadScreenings("screenings.csv");
        assertEquals(3, screenings.size());
        assertEquals("Interstellar", screenings.get(0).getMovieTitle());
        assertEquals(1000, screenings.get(0).getBasePriceCents());
        assertEquals(30, screenings.get(0).getAvailableSeats());
    }

    @Test
    void readsWhitespaceAndSkipsBlankRows() throws IOException {
        List<Screening> screenings = loader.readScreenings(new StringReader(header + "\n 1 , Film , 1000 , 5 \n"));
        assertEquals("Film", screenings.get(0).getMovieTitle());
        assertEquals(5, screenings.get(0).getAvailableSeats());
    }

    @Test
    void missingFileIsReported() {
        assertThrows(IOException.class, () -> loader.loadScreenings("missing.csv"));
    }

    @Test
    void emptyFileOrWrongHeaderIsRejected() {
        assertThrows(IOException.class, () -> loader.readScreenings(new StringReader("")));
        assertThrows(IOException.class, () -> loader.readScreenings(new StringReader("wrong\n1,Film,1000,5")));
        assertThrows(IOException.class, () -> loader.readScreenings(new StringReader(header)));
    }

    @Test
    void invalidNumberIncludesLineNumberAndCause() {
        IOException error = assertThrows(IOException.class,
                () -> loader.readScreenings(new StringReader(header + "1,Film,no,5")));
        assertTrue(error.getMessage().contains("line 2"));
        assertNotNull(error.getCause());
    }

    @Test
    void missingAndExtraColumnsAreRejected() {
        assertThrows(IOException.class, () -> loader.readScreenings(new StringReader(header + "1,Film,1000")));
        assertThrows(IOException.class, () -> loader.readScreenings(new StringReader(header + "1,Film,1000,5,extra")));
    }

    @Test
    void invalidBusinessValuesAreRejected() {
        assertThrows(IOException.class, () -> loader.readScreenings(new StringReader(header + "1,Film,-1,5")));
        assertThrows(IOException.class, () -> loader.readScreenings(new StringReader(header + "1,Film,1000,0")));
        assertThrows(IOException.class, () -> loader.readScreenings(new StringReader(header + "1,,1000,5")));
    }

    @Test
    void duplicateIdsAreRejected() {
        assertThrows(IOException.class, () -> loader.readScreenings(
                new StringReader(header + "1,Film,1000,5\n1,Other,1200,10")));
    }

    @Test
    void quotedFieldsAreRejected() {
        assertThrows(IOException.class, () -> loader.readScreenings(
                new StringReader(header + "1,\"Film\",1000,5")));
    }
}
