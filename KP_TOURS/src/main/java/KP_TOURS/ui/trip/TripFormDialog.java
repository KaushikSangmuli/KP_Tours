package KP_TOURS.ui.trip;

import KP_TOURS.cache.TripCacheManager;
import KP_TOURS.model.Trip;
import KP_TOURS.model.TripStatus;
import KP_TOURS.repository.TripRepository;
import KP_TOURS.util.FileUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;

public class TripFormDialog {

    // =========================================================
    // OPEN ADD FORM
    // =========================================================

    public static void openAddDialog(Runnable refreshCallback) {

        openDialog(
                null,
                refreshCallback
        );
    }

    // =========================================================
    // OPEN EDIT FORM
    // =========================================================

    public static void openEditDialog(
            Trip trip,
            Runnable refreshCallback
    ) {

        openDialog(
                trip,
                refreshCallback
        );
    }

    // =========================================================
    // MAIN DIALOG
    // =========================================================

    private static void openDialog(
            Trip existingTrip,
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
                new VBox(15);

        root.setPadding(new Insets(20));

        // =====================================================
        // FIELDS
        // =====================================================

        DatePicker tripDate =
                new DatePicker();

        tripDate.setValue(LocalDate.now());

        TextField naam =
                input("Naam");

        TextField sector =
                input("Sector");

        TextField airline =
                input("Airline Name");

        TextField sellAmount =
                input("Sell Amount");

        TextField purchaseAmount =
                input("Purchase Amount");

        TextField bookedBy =
                input("Booked By");

        TextField pnr =
                input("PNR Number");

        ComboBox<TripStatus> status =
                new ComboBox<>();

        status.getItems().addAll(
                TripStatus.values()
        );

        status.setPrefWidth(Double.MAX_VALUE);

        status.setPromptText(
                "Select Status"
        );

        // =====================================================
        // DOCUMENT
        // =====================================================

        Label documentLabel =
                new Label("No File Selected");

        final File[] selectedDocument =
                new File[1];

        Button uploadButton =
                new Button("Upload Document");

        uploadButton.setOnAction(e -> {

            FileChooser chooser =
                    new FileChooser();

            File file =
                    chooser.showOpenDialog(stage);

            if (file != null) {

                selectedDocument[0] = file;

                documentLabel.setText(
                        file.getName()
                );
            }
        });

        // =====================================================
        // PREFILL FOR EDIT
        // =====================================================

        if (existingTrip != null) {

            tripDate.setValue(
                    existingTrip.getTripDate()
            );

            naam.setText(
                    existingTrip.getNaam()
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

            bookedBy.setText(
                    existingTrip.getBookedBy()
            );

            pnr.setText(
                    existingTrip.getPnrNo()
            );

            status.setValue(
                    existingTrip.getStatus()
            );

            documentLabel.setText(
                    existingTrip.getDocumentPath()
                            == null
                            ? "No File"
                            : "Document Attached"
            );
        }

        // =====================================================
        // BUTTONS
        // =====================================================

        Button saveButton =
                new Button("Save");

        Button cancelButton =
                new Button("Cancel");

        saveButton.setOnAction(e -> {

            try {

                TripRepository repository =
                        new TripRepository();

                Trip trip =
                        existingTrip == null
                                ? new Trip()
                                : existingTrip;

                trip.setTripDate(
                        tripDate.getValue()
                );

                trip.setNaam(
                        naam.getText()
                );

                trip.setSector(
                        sector.getText()
                );

                trip.setAirlineName(
                        airline.getText()
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
                        bookedBy.getText()
                );

                trip.setPnrNo(
                        pnr.getText()
                );

                trip.setStatus(
                        status.getValue()
                );

                // =============================================
                // SAVE DOCUMENT
                // =============================================

                if (selectedDocument[0] != null) {

                    String savedPath =
                            FileUtil.saveDocument(
                                    selectedDocument[0]
                            );

                    trip.setDocumentPath(
                            savedPath
                    );
                }

                // =============================================
                // DB
                // =============================================

                if (existingTrip == null) {

                    repository.save(trip);

                    TripCacheManager.addTrip(trip);

                } else {

                    repository.update(trip);

                    TripCacheManager.updateTrip(trip);
                }

                // =============================================
                // REFRESH UI
                // =============================================

                if (refreshCallback != null) {

                    refreshCallback.run();
                }

                stage.close();

            } catch (Exception ex) {

                ex.printStackTrace();

                Alert alert =
                        new Alert(
                                Alert.AlertType.ERROR
                        );

                alert.setContentText(
                        "Failed to save trip"
                );

                alert.showAndWait();
            }
        });

        cancelButton.setOnAction(e -> {

            stage.close();
        });

        HBox buttons =
                new HBox(
                        10,
                        saveButton,
                        cancelButton
                );

        buttons.setAlignment(
                Pos.CENTER_RIGHT
        );

        root.getChildren().addAll(
                new Label("Trip Date"),
                tripDate,

                naam,
                sector,
                airline,
                sellAmount,
                purchaseAmount,
                bookedBy,
                pnr,
                status,

                uploadButton,
                documentLabel,

                buttons
        );

        Scene scene =
                new Scene(
                        root,
                        500,
                        700
                );

        stage.setScene(scene);

        stage.showAndWait();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private static TextField input(String prompt) {

        TextField field =
                new TextField();

        field.setPromptText(prompt);

        return field;
    }

    private static double parseDouble(String value) {

        try {

            return Double.parseDouble(value);

        } catch (Exception e) {

            return 0;
        }
    }
}