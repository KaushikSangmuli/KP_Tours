package com.prabal.app.ui.trip;
@FXML
private TextField naamField;

@FXML
private TextField sectorField;

@FXML
private TextField airlineField;

@FXML
private TextField sellAmountField;

@FXML
private TextField purchaseAmountField;

@FXML
private TextField bookedByField;

@FXML
private TextField pnrField;

@FXML
private ComboBox<TripStatus> statusComboBox;

@FXML
private Label documentNameLabel;

private File selectedDocument;

@FXML
public void initialize() {

    statusComboBox.getItems().addAll(
            TripStatus.values()
    );
}

@FXML
private void onUploadDocument() {

    FileChooser fileChooser = new FileChooser();

    selectedDocument = fileChooser.showOpenDialog(null);

    if (selectedDocument != null) {

        documentNameLabel.setText(
                selectedDocument.getName()
        );
    }
}

@FXML
private void onSave() {

    System.out.println("Save Trip");
}

@FXML
private void onCancel() {

    System.out.println("Close Form");
}
}