# Cinema Ticket Booking

A small Java console project: show screenings, book standard or student tickets, cancel bookings, and view revenue. It is designed around a familiar process and the OOP concepts in the JUNIA M1 course.

## The whole idea in one example

Interstellar has 30 seats. A standard ticket costs 10 EUR. A student ticket costs 8 EUR.

1. Amine books two student tickets.
2. The total is 16 EUR, and 28 seats remain.
3. The booking receives an ID, such as 1.
4. Cancelling booking 1 restores the two seats and removes 16 EUR from revenue.
5. The cancelled booking remains visible in history. Cancelling it again is refused.

There are no seat numbers, calendar calculations, real payments, accounts or databases.

## Run the project

Requirements: JDK 17 or newer and Maven 3.6.3 or newer. The only dependency is JUnit 5 for tests. Maven needs internet access for dependencies on the first build.

Open **this folder's pom.xml** in IntelliJ as a separate project, then run `com.cinema.Main`. Do not open the tourism project's pom.xml to run this application.

Or open a terminal in this folder:

```sh
mvn clean verify
java -jar target/cinema-ticket-booking-1.0-SNAPSHOT.jar
```

The CSV is included in the JAR, so the application does not depend on the working directory. Rebuild after changing the CSV. If `mvn` is not available in your terminal, run `clean` and `verify` using IntelliJ's Maven tool window.

## Menu

1. **Show screenings:** movie title, standard/student price and available seats.
2. **Book tickets:** screening ID, customer name, ticket type and quantity. Enter 0 at the screening prompt to go back.
3. **Cancel a booking:** enter its booking ID. Enter 0 to go back.
4. **Show bookings and revenue:** active and cancelled bookings, plus the revenue after refunds.
5. **Exit.**

A customer may make more than one booking. Each booking has its own ID. A booking contains one ticket type and one screening; use a second booking for a different type. Student status is entered by the operator, without document verification. All data resets when the application restarts.

## Classes and reading order

There are **10 main classes**:

| Read | Class | Responsibility |
| --- | --- | --- |
| 1 | `Screening` | Movie title, base price, capacity and seat availability |
| 2 | `Ticket` | Abstract parent with a screening and price/type methods |
| 3 | `StandardTicket` | Full-price ticket |
| 4 | `StudentTicket` | Ticket with a 20% discount |
| 5 | `Booking` | Customer, ticket, quantity, total and cancellation state |
| 6 | `BookingException` | Explains a refused booking or cancellation |
| 7 | `BookingService` | Finds screenings, creates bookings, handles cancellations and calculates revenue |
| 8 | `CsvDataLoader` | Reads screenings from CSV |
| 9 | `ConsoleMenu` | Reads input and prints the menu and results |
| 10 | `Main` | Loads the file and starts the menu |

Source packages are `model`, `service`, `repository`, `exception` and `ui`, under `src/main/java/com/cinema`. Tests use the same layout under `src/test/java/com/cinema`.

### Follow one booking in the code

`ConsoleMenu.bookTickets()` collects input and calls `BookingService.bookTickets()`.

The service finds the screening and checks the name, quantity and available seats. It creates either a StandardTicket or StudentTicket, storing it in a variable of type Ticket.

It then creates a Booking. The Booking calls `ticket.getPriceCents()` and multiplies by the quantity. Java chooses the price method of the actual ticket subclass. After validating its fields, the Booking reserves the seats. The service adds it to history and returns it to the menu.

Cancellation calls `Booking.cancel()`. It checks that cancellation has not happened already, restores seats and marks the booking cancelled. Revenue is the sum of bookings that are still active.

## Course requirements

- **Encapsulation:** private fields and controlled methods such as `reserveSeats` and `cancel`; no public setters for seat counts.
- **Inheritance:** StandardTicket and StudentTicket extend Ticket.
- **Runtime polymorphism:** Booking calls `getPriceCents()` through a Ticket reference; each subclass provides its own calculation.
- **Abstract class:** Ticket shares the screening reference and requires subclasses to implement price and type. The assignment allows an abstract class and/or an interface, so no extra interface is needed.
- **Business logic:** seat availability, price calculation, discounts, cancellation, refunds and booking history.
- **Automatic file loading:** Main loads `screenings.csv` before showing the menu.
- **Exceptions:** BookingException is a checked custom exception. IOException handles file errors; IllegalArgumentException and IllegalStateException protect model rules. The menu handles expected errors with `try/catch`.
- **Collections and generics:** List<Screening>, List<Booking> and Set<Integer>. Optional<Screening> represents a search that might find nothing.
- **JUnit 5:** normal cases, boundary cases and invalid requests are tested.

### SOLID, without extra frameworks

- **SRP:** reading files, interacting with the user and managing bookings have separate classes.
- **OCP:** Booking calculates a total through Ticket; new ticket pricing subclasses do not require changing that calculation. The service's creation choice would still need updating to offer a new type.
- **LSP:** either ticket subtype can be used by Booking with the same contract: it has a screening, a price in cents and a type description.
- **ISP:** Ticket exposes a small pricing contract; no large multipurpose interface is introduced.
- **DIP:** Booking uses the abstract Ticket type rather than a particular ticket subtype. Constructors receive collaborators directly; there is no injection framework.

These are practical examples, not a claim that every class uses every principle.

## Four design decisions

1. **Use cents for money.** The CSV stores 1000 for 10.00 EUR. Student tickets cost 80% of the base price, rounded down to the nearest cent. Booking totals use long to avoid int multiplication overflow. Display formatting does not use floating-point money calculations.
2. **Count available seats instead of choosing seat numbers.** This keeps the central rule easy: a booking cannot exceed available seats.
3. **Keep cancelled bookings.** History remains visible, but cancelled bookings do not contribute to revenue. A cancellation can happen only once.
4. **Separate console and business logic.** Tests can book and cancel without typing into a console. The same service is used by the menu and tests.

## CSV format

The bundled file is `src/main/resources/screenings.csv`:

```csv
id,movie,priceCents,seats
1,Interstellar,1000,30
2,Spider-Man,1250,25
3,The Lion King,800,20
```

IDs, prices and capacities must be positive. IDs must be unique. Titles cannot be blank or contain commas or quotation marks. The header must match exactly. Blank data lines are skipped, and whitespace around data values is trimmed. Missing files, invalid columns, invalid values and duplicate IDs produce clear errors; invalid rows include a line number.

The loader follows the basic comma-splitting approach in the [course lab corrections](https://github.com/AdriMareel/OOP-Java-M1/tree/main/src/main/java/com/junia/class5/file). Classes, constructors, overridden methods, loops and tests follow the same course concepts. No lab source files are copied into this project.

## Test and build results

```sh
mvn test
mvn clean verify
```

Verified on 29 October 2026:

- **BUILD SUCCESS**.
- **34 tests passed, 0 failures, 0 errors, 0 skipped.**
- Tested booking prices, student discounts, sold-out screenings, invalid requests, seat restoration, duplicate cancellation, revenue, CSV errors and console input.
- The packaged JAR was run from another directory: two student tickets cost 16 EUR, cancellation restored the seats and reduced revenue to zero, and the process exited successfully.

Verification used the installed OpenJDK 27 with compiler target Java 17. A separate Java 17 runtime was not tested.

## Submission and limitations

This is a standalone Maven project inside the `Cinema ticket booking` folder. It does not depend on Earth Tourism Agency. You may copy this entire folder to another location and open its pom.xml there.

For GitHub, submit this folder's source, tests, CSV, pom.xml, README and .gitignore. The ignore file excludes target and IDE settings. This folder has not been published or initialized as a separate Git repository.

Scope: one console user, in-memory bookings, no authentication, no real payment/refund processing, no assigned seats or screening dates. The revenue is a simulation. Classes assume sequential use by this application, not concurrent bookings.
