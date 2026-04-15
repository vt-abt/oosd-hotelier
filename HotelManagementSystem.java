package com.example.demo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class HotelManagementSystem extends Application {
    private final ObservableList<Room> allRooms = FXCollections.observableArrayList();
    private final Map<String, String> floorManagers = new HashMap<>();
    private final ObservableList<String> staffPool = FXCollections.observableArrayList(
            "Mr. Lawrence", "Ms. Shweta", "Ms. Leyla", "Mr. Faarukh", "Ms. Vanita"
    );

    private BarChart<String, Number> revenueChart;
    private XYChart.Series<String, Number> chartSeries;
    private VBox analyticsNumbers;
    private boolean isHeatMapActive = false;
    private VBox floorDisplayContainer;
    private ListView<String> timelineList = new ListView<>();

    @Override
    public void start(Stage stage) {
        initData();
        TabPane root = new TabPane();
        root.getTabs().addAll(
                new Tab("Live Dashboard", createLandingTab()),
                new Tab("Operations & Timeline", createOperationsTab()),
                new Tab("Checkout & Billing", createCheckoutTab()),
                new Tab("Admin & Staffing", createAdminTab()),
                new Tab("Analytics", createAnalyticsTab())
        );
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Scene scene = new Scene(root, 1280, 900);
        scene.getRoot().setStyle("-fx-font-family: 'Segoe UI'; -fx-base: #ffffff;");
        stage.setTitle("HMS Enterprise - Corporate Hotelier Suite");
        stage.setScene(scene);
        stage.show();
    }

    private void initData() {
        floorManagers.put("GF", staffPool.get(0));
        floorManagers.put("FF", staffPool.get(1));
        floorManagers.put("SF", staffPool.get(2));

        for (int i : new int[]{0, 100})
            for (int j = 1; j <= 20; j++) allRooms.add(new Room(i+j, Room.Type.DELUXE));
        for (int j : new int[]{21,22,23,24,25, 121,122,123,124,125,126,127,128,129})
            allRooms.add(new Room(j, Room.Type.SUPER_DELUXE));
        for (int j = 201; j <= 206; j++) allRooms.add(new Room(j, Room.Type.ALSACE_SUITE));

        injectMockStateForPresentation();
    }

    private void injectMockStateForPresentation() {
        LocalDate today = LocalDate.now();

        for (Room r : allRooms) {
            int n = r.getNumber();
            double bRate = r.getType().base;
            double pRate = r.getType().pre;

            if (n % 3 == 0) {
                Booking past1 = new Booking("Corp Client " + n, "ID-C" + n, "555-0000", today.minusDays(25), today.minusDays(22), 1, bRate);
                past1.setCheckoutCompleted(true);
                r.addBooking(past1);
            }
            if (n % 4 == 0) {
                Booking past2 = new Booking("Tour Group " + n, "ID-T" + n, "555-1111", today.minusDays(15), today.minusDays(10), 2, bRate);
                past2.setCheckoutCompleted(true);
                r.addBooking(past2);
            }

            switch(n) {
                case 11:
                case 121:
                    r.addBooking(new Booking("Overdue Guest", "ID-OVER", "555-9111", today.minusDays(5), today.minusDays(1), 2, bRate));
                    r.setStatus(Room.Status.OCCUPIED);
                    break;

                case 4:
                case 201:
                    r.addBooking(new Booking("Current VIP", "ID-VIP1", "555-0101", today.minusDays(2), today.plusDays(3), 1, bRate));
                    r.setStatus(Room.Status.OCCUPIED);
                    break;

                case 22:
                case 105:
                    r.addBooking(new Booking("Departing Guest", "ID-BYE", "555-2020", today.minusDays(4), today, 1, bRate));
                    r.setStatus(Room.Status.OCCUPIED);
                    break;

                case 204:
                    r.addBooking(new Booking("New Arrival", "ID-NEW", "555-3030", today, today.plusDays(2), 2, bRate));
                    r.setStatus(Room.Status.OCCUPIED);
                    break;

                case 18:
                case 125:
                    r.addBooking(new Booking("Future Bride", "ID-WED", "555-4040", today.plusDays(3), today.plusDays(7), 4, pRate));
                    r.setStatus(Room.Status.PREBOOKED);
                    break;

                case 8:
                case 206:
                    r.setStatus(Room.Status.VACANT);
                    r.setNeedsCleaning(true);
                    break;

                case 13:
                case 129:
                    r.setStatus(Room.Status.MAINTENANCE);
                    break;

                case 25:
                    Booking old = new Booking("Mr. Past", "ID-OLD", "555-7777", today.minusDays(10), today.minusDays(5), 1, bRate);
                    old.setCheckoutCompleted(true);
                    r.addBooking(old);
                    r.addBooking(new Booking("Ms. Present", "ID-NOW", "555-8888", today.minusDays(1), today.plusDays(1), 1, bRate));
                    r.setStatus(Room.Status.OCCUPIED);
                    r.addBooking(new Booking("Dr. Future", "ID-FUT", "555-9999", today.plusDays(3), today.plusDays(6), 1, pRate));
                    break;
            }
        }
    }

    private ScrollPane createLandingTab() {
        floorDisplayContainer = new VBox(30);
        floorDisplayContainer.setPadding(new Insets(20));
        ToggleButton heatMapToggle = new ToggleButton("Toggle Strategic Profit Heat Map");
        heatMapToggle.setStyle("-fx-font-weight: bold; -fx-cursor: hand;");
        heatMapToggle.setOnAction(e -> { isHeatMapActive = heatMapToggle.isSelected(); refreshFloorDisplay(); });

        HBox legend = new HBox(15,
                new Label("🟩 Vacant"), new Label("🟥 Occupied / Overstay"),
                new Label("🟨 Pre-booked"), new Label("⬜ Maintenance"), new Label("🧹 Needs Cleaning")
        );
        legend.setStyle("-fx-padding: 10 0; -fx-font-weight: bold;");

        refreshFloorDisplay();
        return new ScrollPane(new VBox(10, new HBox(20, heatMapToggle, legend), floorDisplayContainer));
    }

    private void refreshFloorDisplay() {
        floorDisplayContainer.getChildren().clear();
        String[][] floors = {{"GF", "Ground Floor"}, {"FF", "First Floor"}, {"SF", "Second Floor"}};
        for (String[] f : floors) {
            VBox floorBox = new VBox(10);
            Label header = new Label(f[1] + " | Duty Manager: " + floorManagers.get(f[0]));
            header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            FlowPane grid = new FlowPane(8, 8);
            for (Room r : allRooms) if (isRoomOnFloor(r, f[0])) grid.getChildren().add(createRoomCard(r));
            floorBox.getChildren().addAll(header, grid, new Separator());
            floorDisplayContainer.getChildren().add(floorBox);
        }
    }

    private boolean isRoomOnFloor(Room r, String code) {
        int n = r.getNumber();
        if (code.equals("GF")) return n < 100;
        if (code.equals("FF")) return n >= 100 && n < 200;
        return n >= 200;
    }

    private VBox createRoomCard(Room r) {
        VBox card = new VBox(2);
        card.setPrefSize(100, 60); card.setAlignment(Pos.CENTER);

        String color = r.isOverstaying() ? "#8B0000" : (isHeatMapActive ? r.getType().heatColor :
                                                        switch (r.getStatus()) {
                                                            case VACANT -> "#27ae60"; case OCCUPIED -> "#c0392b";
                                                            case PREBOOKED -> "#f1c40f"; default -> "#7f8c8d";
                                                        });

        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4; -fx-cursor: hand;");

        Label lbl = new Label("R-" + r.getNumber());
        lbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label dirtyLbl = new Label(r.isNeedsCleaning() ? "🧹 Dirty" : "");
        dirtyLbl.setStyle("-fx-text-fill: white; -fx-font-size: 10px;");

        card.getChildren().addAll(lbl, dirtyLbl);
        card.setOnMouseClicked(e -> showRoomDetail(r));
        return card;
    }

    private void showRoomDetail(Room r) {
        Booking b = r.getActiveBooking();
        String msg = (b != null) ? String.format("Guest: %s\nID: %s\nStay: %s to %s\nBill: ₹%.2f %s",
                b.getName(), b.getIdCard(), b.getCheckIn(), b.getCheckOut(), b.getTotalAmt(),
                r.isOverstaying() ? "\n\n[ALERT: GUEST IS OVERDUE]" : "")
                : "Room is currently " + r.getStatus().name() + ".\nNeeds Cleaning: " + r.isNeedsCleaning();
        showAlert("Room " + r.getNumber() + " Dossier", msg, Alert.AlertType.INFORMATION);
    }

    private HBox createOperationsTab() {
        GridPane form = new GridPane();
        form.setPadding(new Insets(20)); form.setHgap(15); form.setVgap(15);

        TextField nameF = new TextField(), idF = new TextField();
        DatePicker inP = new DatePicker(LocalDate.now()), outP = new DatePicker(LocalDate.now().plusDays(1));

        ComboBox<Room> roomCombo = new ComboBox<>(allRooms);
        roomCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty ? "" : "Room " + r.getNumber() + " (" + r.getType() + ")");
            }
        });

        Button btn = new Button("Confirm Booking");
        btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setOnAction(e -> {
            Room r = roomCombo.getValue();
            LocalDate in = inP.getValue(), out = outP.getValue();

            if (nameF.getText().isEmpty() || r == null || in == null || out == null || !out.isAfter(in)) {
                showAlert("Validation Error", "Please fill all fields and ensure Check-Out is after Check-In.", Alert.AlertType.ERROR);
                return;
            }

            if (r.isAvailable(in, out)) {
                double rate = in.isAfter(LocalDate.now()) ? r.getType().pre : r.getType().base;
                r.addBooking(new Booking(nameF.getText(), idF.getText(), "", in, out, 1, rate));

                if (in.isAfter(LocalDate.now())) r.setStatus(Room.Status.PREBOOKED);
                else r.setStatus(Room.Status.OCCUPIED);

                refreshFloorDisplay(); updateTimeline(); updateAnalytics();
                nameF.clear(); idF.clear();
                showAlert("Success", "Booking Registered Successfully.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Conflict", "Room is unavailable (Occupied, Maintenance, or Needs Cleaning).", Alert.AlertType.WARNING);
            }
        });

        form.addRow(0, new Label("Guest Name:"), nameF); form.addRow(1, new Label("Aadhaar/ID:"), idF);
        form.addRow(2, new Label("Check-in:"), inP); form.addRow(3, new Label("Check-out:"), outP);
        form.addRow(4, new Label("Select Room:"), roomCombo); form.add(btn, 1, 5);

        timelineList.setPrefWidth(500);
        updateTimeline();
        return new HBox(20, new VBox(10, new Label("Reservation Desk"), form), new VBox(10, new Label("Operations Timeline"), timelineList));
    }

    private void updateTimeline() {
        timelineList.getItems().clear();
        allRooms.forEach(r -> r.getBookings().forEach(b -> {
            if (!b.isCheckoutCompleted()) {
                timelineList.getItems().add(String.format("R-%d | %s | %s to %s %s",
                        r.getNumber(), b.getName(), b.getCheckIn(), b.getCheckOut(),
                        r.isOverstaying() ? "[OVERSTAY]" : ""));
            }
        }));
    }

    private VBox createCheckoutTab() {
        TableView<Room> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().add(createColumn("Room #", r -> r.numberProperty().asObject()));
        table.getColumns().add(createColumn("Guest Name", r -> {
            Booking b = r.getActiveBooking();
            return new javafx.beans.property.SimpleStringProperty(b != null ? b.getName() : "N/A");
        }));
        table.getColumns().add(createColumn("Total Bill", r -> {
            Booking b = r.getActiveBooking();
            return new javafx.beans.property.SimpleStringProperty(b != null ? "₹" + String.format("%.2f", b.getTotalAmt()) : "₹0.00");
        }));

        Runnable syncData = () -> {
            ObservableList<Room> active = FXCollections.observableArrayList();
            for (Room r : allRooms) if (r.getStatus() == Room.Status.OCCUPIED || r.isOverstaying()) active.add(r);
            table.setItems(active);
        };

        Button syncBtn = new Button("🔄 Refresh Occupancy List");
        syncBtn.setOnAction(e -> syncData.run());

        Button btn = new Button("Process Checkout & Generate Invoice");
        btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r != null) {
                Booking b = r.getActiveBooking();
                showAlert("Invoice Finalized", "Guest: " + b.getName() + "\nFinal Bill: ₹" + b.getTotalAmt() + "\n\nRoom dispatched to Housekeeping.", Alert.AlertType.INFORMATION);
                r.processCheckout();
                syncData.run(); refreshFloorDisplay(); updateTimeline(); updateAnalytics();
            } else {
                showAlert("Error", "Please select a room to checkout.", Alert.AlertType.WARNING);
            }
        });

        table.setOnMouseClicked(e -> syncData.run());
        syncData.run();

        return new VBox(15, new Label("Billing Terminal"), new HBox(10, syncBtn), table, btn);
    }

    private VBox createAdminTab() {
        VBox layout = new VBox(20); layout.setPadding(new Insets(20));

        ComboBox<String> fSel = new ComboBox<>(FXCollections.observableArrayList("GF", "FF", "SF"));
        ComboBox<String> mSel = new ComboBox<>(staffPool);
        Button btn = new Button("Deploy Staff");
        btn.setOnAction(e -> {
            if(fSel.getValue() != null && mSel.getValue() != null) {
                floorManagers.put(fSel.getValue(), mSel.getValue()); refreshFloorDisplay();
            }
        });

        TableView<Room> table = new TableView<>(allRooms);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().add(createColumn("Room #", r -> r.numberProperty().asObject()));
        table.getColumns().add(createColumn("Needs Cleaning?", r -> r.needsCleaningProperty()));

        Button cleanBtn = new Button("Mark Selected Room as Cleaned");
        cleanBtn.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r != null) { r.setNeedsCleaning(false); refreshFloorDisplay(); table.refresh(); }
        });

        layout.getChildren().addAll(new Label("Personnel Command"), new HBox(10, fSel, mSel, btn), new Separator(), new Label("Housekeeping Register"), table, cleanBtn);
        return layout;
    }

    private HBox createAnalyticsTab() {
        analyticsNumbers = new VBox(15); analyticsNumbers.setPadding(new Insets(20));
        analyticsNumbers.setMinWidth(250);

        CategoryAxis xAxis = new CategoryAxis(); NumberAxis yAxis = new NumberAxis();
        revenueChart = new BarChart<>(xAxis, yAxis);
        revenueChart.setAnimated(false);
        chartSeries = new XYChart.Series<>();
        chartSeries.setName("30-Day Revenue Validation (INR)");
        revenueChart.getData().add(chartSeries);

        updateAnalytics();
        return new HBox(20, analyticsNumbers, revenueChart);
    }

    private void updateAnalytics() {
        analyticsNumbers.getChildren().clear();
        chartSeries.getData().clear();
        analyticsNumbers.getChildren().add(new Label("Segment Performance"));
        String[] types = {"DELUXE", "SUPER_DELUXE", "ALSACE_SUITE"};
        for (String t : types) {
            double rev = calculateRevenue(t, 30);
            Label l = new Label(t + ": ₹" + String.format("%.2f", rev));
            l.setStyle("-fx-font-weight: bold;");
            analyticsNumbers.getChildren().add(l);
            chartSeries.getData().add(new XYChart.Data<>(t, rev));
        }
    }

    private double calculateRevenue(String type, int days) {
        LocalDate limit = LocalDate.now().minusDays(days);
        return allRooms.stream().filter(r -> r.getType().name().equals(type))
                .flatMap(r -> r.getBookings().stream())
                .filter(b -> !b.getCheckIn().isBefore(limit))
                .mapToDouble(b -> b.getTotalAmt()).sum();
    }

    private <T> TableColumn<Room, T> createColumn(String n, java.util.function.Function<Room, javafx.beans.value.ObservableValue<T>> f) {
        TableColumn<Room, T> c = new TableColumn<>(n); c.setCellValueFactory(cd -> f.apply(cd.getValue())); return c;
    }

    private void showAlert(String t, String m, Alert.AlertType at) {
        Alert a = new Alert(at); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}