package KP_TOURS.ui.settings;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AppSettings {

    private static final StringProperty businessName =
            new SimpleStringProperty("KP Tours");

    private AppSettings() {
    }

    public static StringProperty businessNameProperty() {
        return businessName;
    }

    public static String getBusinessName() {
        return businessName.get();
    }

    public static void setBusinessName(String name) {

        if (name == null || name.isBlank()) {
            businessName.set("Admin");
            return;
        }

        businessName.set(name.trim());
    }
}