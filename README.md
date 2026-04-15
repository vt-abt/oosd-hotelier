# Hotelier (Beta)

An enterprise-grade Facility Management and Business Intelligence suite built for the desktop. Hotelier (Beta) utilizes a strict MVC architecture to provide a real-time, zero-latency reactive UI for hotel operations, bypassing the limitations of traditional monolithic legacy systems.

## Core Architecture

* **Framework:** JavaFX
* **Language:** Java
* **Design Pattern:** Model-View-Controller (MVC)
* **State Management:** In-memory reactive data binding (ObservableList, ObjectProperty) designed for eventual RDBMS (PostgreSQL/MongoDB) integration.

## Key Features

* **Live Dashboard & Strategic Heat Map:** Real-time visual grid of all property assets. Includes conditional state rendering (Vacant, Occupied, Prebooked, Maintenance, Needs Cleaning) and a profitability thermal map toggle.
* **Conflict-Aware Booking Engine:** Implements temporal mathematics (`java.time`) to physically block double-bookings, overlapping dates, and assignments to unserviced rooms.
* **Synchronized Operations Timeline:** A scrolling sequential list rendering all active and scheduled reservations alongside the booking interface.
* **Automated Checkout & Billing:** Filtered data grids for active guests. Calculates final invoices based on stay duration and automatically dispatches vacated rooms to the Housekeeping register.
* **Admin Personnel System:** Dynamic deployment of Duty Managers using `HashMap` data structures, allowing real-time administrative overrides.
* **Business Intelligence (BI) Analytics:** A 30-day segment performance analyzer that projects raw transactional data into real-time visual charts.

## Edge Case Mitigation

* **Early Checkout Ghosting:** Implements a `checkoutCompleted` flag to truncate original booking dates and instantly free inventory.
* **Same-Day Turnover:** Temporal validation algorithms permit same-day check-ins while strictly blocking overnight overlaps.
* **Defensive Allocation:** System-level locks prevent assigning guests to rooms flagged for maintenance or cleaning.

## Installation & Execution

Ensure you have a JDK (17 or higher) and Maven installed on your system.

1. Clone the repository.
```bash
git clone https://github.com/yourusername/hotelier-beta.git
cd hotelier-beta
```

2. Compile and run using Maven.
```bash
mvn clean javafx:run
```

## Project Structure

* `src/main/java/com/example/demo/HotelManagementSystem.java`: The primary View-Controller bridging the UI components and the data state.
* `src/main/java/com/example/demo/Room.java`: The core Data Model encapsulating inventory logic, pricing tiers, and temporal validation methods.
* `src/main/java/com/example/demo/Booking.java`: The Data Model handling guest records, duration mathematics, and operational state flags.
