package KP_TOURS.ui.trip;

import KP_TOURS.cache.TripCacheManager;
import KP_TOURS.model.Trip;
import KP_TOURS.model.TripDocument;
import KP_TOURS.model.TripStatus;
import KP_TOURS.repository.TripDocumentRepository;
import KP_TOURS.repository.TripRepository;
import KP_TOURS.util.FileUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TripFormDialog {

    public static void openAddDialog(
            LocalDate selectedDate,
            Runnable refreshCallback
    ) {

        openDialog(
                null,
                selectedDate,
                refreshCallback
        );
    }

    public static void openEditDialog(
            Trip trip,
            Runnable refreshCallback
    ) {

        openDialog(
                trip,
                trip.getTripDate(),
                refreshCallback
        );
    }

    private static void openDialog(
            Trip existingTrip,
            LocalDate defaultDate,
            Runnable refreshCallback
    ) {

        Stage stage =
                new Stage();

        stage.initModality(
                Modality.APPLICATION_MODAL
        );

        stage.setTitle(
                existingTrip == null
                        ? "Add Trip"
                        : "Edit Trip"
        );

        VBox root =
                new VBox(18);

        root.setPadding(
                new Insets(28)
        );

        root.getStyleClass()
                .add("trip-form-root");

        VBox header =
                new VBox(4);

        Label title =
                new Label(
                        existingTrip == null
                                ? "Add New Trip"
                                : "Edit Trip"
                );

        title.getStyleClass()
                .add("trip-form-title");

        Label subtitle =
                new Label(
                        "Enter booking details and attach related documents."
                );

        subtitle.getStyleClass()
                .add("trip-form-subtitle");

        header.getChildren().addAll(
                title,
                subtitle
        );

        DatePicker tripDate =
                new DatePicker();

        tripDate.setValue(
                defaultDate == null
                        ? LocalDate.now()
                        : defaultDate
        );

        tripDate.getStyleClass()
                .add("premium-input");

        TextField name =
                input("Name");

        TextField sector =
                input("Sector");

        TextField airline =
                input("Airline Name");

        TextField sellAmount =
                input("Sell Amount");

        TextField purchaseAmount =
                input("Purchase Amount");

        ComboBox<String> bookedBy =
                new ComboBox<>();

        bookedBy.getItems().addAll(
                "Cash",
                "Credit",
                "Card"
        );

        bookedBy.setPromptText(
                "Select Booked By"
        );

        bookedBy.setMaxWidth(
                Double.MAX_VALUE
        );

        bookedBy.getStyleClass()
                .add("premium-input");

        TextField pnr =
                input("PNR Number");

        ComboBox<TripStatus> status =
                new ComboBox<>();

        status.getItems().addAll(
                TripStatus.values()
        );

        status.setPromptText(
                "Select Status"
        );

        status.setMaxWidth(
                Double.MAX_VALUE
        );

        status.getStyleClass()
                .add("premium-input");

        TextArea description =
                new TextArea();

        description.setPromptText(
                "Description"
        );

        description.setPrefRowCount(3);

        description.setWrapText(true);

        description.getStyleClass()
                .add("premium-text-area");

        Label documentLabel =
                new Label("No File Selected");

        documentLabel.getStyleClass()
                .add("document-label");

        List<File> selectedDocuments =
                new ArrayList<>();

        Button uploadButton =
                new Button("Upload Documents");

        uploadButton.getStyleClass()
                .add("secondary-button");

        uploadButton.setOnAction(e -> {

            FileChooser chooser =
                    new FileChooser();

            chooser.setTitle(
                    "Select Documents"
            );

            List<File> files =
                    chooser.showOpenMultipleDialog(stage);

            if (files != null && !files.isEmpty()) {

                selectedDocuments.clear();

                selectedDocuments.addAll(files);

                documentLabel.setText(
                        files.size() + " file(s) selected"
                );
            }
        });

        VBox documentBox =
                new VBox(10);

        documentBox.getChildren().addAll(
                uploadButton,
                documentLabel
        );

        if (existingTrip != null) {

            tripDate.setValue(
                    existingTrip.getTripDate()
            );

            name.setText(
                    existingTrip.getName()
            );

            sector.setText(
                    existingTrip.getSector()
            );

            airline.setText(
                    existingTrip.getAirlineName()
            );

            sellAmount.setText(
                    String.valueOf(
                            existingTrip.getSellAmount()
                    )
            );

            purchaseAmount.setText(
                    String.valueOf(
                            existingTrip.getPurchaseAmount()
                    )
            );

            bookedBy.setValue(
                    existingTrip.getBookedBy()
            );

            pnr.setText(
                    existingTrip.getPnrNo()
            );

            status.setValue(
                    existingTrip.getStatus()
            );

            description.setText(
                    existingTrip.getDescription()
            );
        }

        Button saveButton =
                new Button("Save Trip");

        saveButton.getStyleClass()
                .add("primary-button");

        Button cancelButton =
                new Button("Cancel");

        cancelButton.getStyleClass()
                .add("secondary-button");

        saveButton.setOnAction(e -> {

            try {

                if (name.getText().trim().isEmpty()) {
                    alert("Name is required");
                    return;
                }

                if (sector.getText().trim().isEmpty()) {
                    alert("Sector is required");
                    return;
                }

                if (bookedBy.getValue() == null) {
                    alert("Please select booked by");
                    return;
                }

                if (status.getValue() == null) {
                    alert("Please select status");
                    return;
                }

                Trip trip =
                        existingTrip == null
                                ? new Trip()
                                : existingTrip;

                trip.setTripDate(
                        tripDate.getValue()
                );

                trip.setName(
                        name.getText().trim()
                );

                trip.setSector(
                        sector.getText().trim()
                );

                trip.setAirlineName(
                        airline.getText().trim()
                );

                trip.setSellAmount(
                        parseDouble(
                                sellAmount.getText()
                        )
                );

                trip.setPurchaseAmount(
                        parseDouble(
                                purchaseAmount.getText()
                        )
                );

                trip.setBookedBy(
                        bookedBy.getValue()
                );

                trip.setPnrNo(
                        pnr.getText().trim()
                );

                trip.setStatus(
                        status.getValue()
                );

                trip.setDescription(
                        description.getText() == null
                                ? null
                                : description.getText().trim()
                );

                TripRepository tripRepository =
                        new TripRepository();

                if (existingTrip == null) {

                    tripRepository.save(trip);

                    TripCacheManager.addTrip(trip);

                } else {

                    tripRepository.update(trip);

                    TripCacheManager.updateTrip(trip);
                }

                if (!selectedDocuments.isEmpty()) {

                    TripDocumentRepository documentRepository =
                            new TripDocumentRepository();

                    for (File file : selectedDocuments) {

                        String savedPath =
                                FileUtil.saveDocument(
                                        file,
                                        trip
                                );

                        if (savedPath == null) {
                            continue;
                        }

                        TripDocument document =
                                new TripDocument();

                        document.setTripUuid(
                                trip.getId()
                        );

                        document.setFileName(
                                new File(savedPath).getName()
                        );

                        document.setFilePath(
                                savedPath
                        );

                        documentRepository.save(
                                document
                        );
                    }
                }

                if (refreshCallback != null) {
                    refreshCallback.run();
                }

                stage.close();

            } catch (Exception ex) {

                ex.printStackTrace();

                alert("Failed to save trip");
            }
        });

        cancelButton.setOnAction(e ->
                stage.close()
        );

        HBox buttons =
                new HBox(
                        12,
                        cancelButton,
                        saveButton
                );

        buttons.setAlignment(
                Pos.CENTER_RIGHT
        );

        root.getChildren().addAll(
                header,
                field("Trip Date", tripDate),
                field("Name", name),
                field("Sector", sector),
                field("Airline Name", airline),
                field("Sell Amount", sellAmount),
                field("Purchase Amount", purchaseAmount),
                field("Booked By", bookedBy),
                field("PNR Number", pnr),
                field("Status", status),
                field("Description", description),
                field("Documents", documentBox),
                buttons
        );

        ScrollPane scrollPane =
                new ScrollPane(root);

        scrollPane.setFitToWidth(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollPane.getStyleClass()
                .add("trip-form-scroll");

        Scene scene =
                new Scene(
                        scrollPane,
                        540,
                        720
                );

        scene.getStylesheets().add(
                TripFormDialog.class
                        .getResource("/css/app.css")
                        .toExternalForm()
        );

        stage.setScene(scene);

        stage.showAndWait();
    }

    private static VBox field(
            String labelText,
            Node node
    ) {

        VBox box =
                new VBox(8);

        Label label =
                new Label(labelText);

        label.getStyleClass()
                .add("field-title");

        box.getChildren().addAll(
                label,
                node
        );

        return box;
    }

    private static TextField input(
            String prompt
    ) {

        TextField field =
                new TextField();

        field.setPromptText(
                prompt
        );

        field.getStyleClass()
                .add("premium-input");

        return field;
    }

    private static double parseDouble(
            String value
    ) {

        try {

            return Double.parseDouble(
                    value
            );

        } catch (Exception e) {

            return 0;
        }
    }

    private static void alert(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }
}