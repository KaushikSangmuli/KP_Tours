package KP_TOURS.ui.sidebar;

import KP_TOURS.ui.accounts.AccountView;
import KP_TOURS.ui.dashboard.DashboardView;
import KP_TOURS.ui.purchasesales.PurchaseSalesView;
import KP_TOURS.ui.settings.AppSettings;
import KP_TOURS.ui.settings.SettingsView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SidebarView {

    private static Button activeButton;


    private SidebarView() {
    }

    public static VBox getView() {

        VBox sidebar = new VBox(14);

        sidebar.setPrefWidth(230);
        sidebar.setPadding(new Insets(24));
        sidebar.getStyleClass().add("sidebar");
        HBox branding = new HBox(10);
        branding.setAlignment(Pos.CENTER_LEFT);

        ImageView logo = new ImageView(
               new Image( SidebarView.class
                        .getResourceAsStream("/icons/logo.png")
        )
        );

        logo.setFitWidth(30);
        logo.setFitHeight(30);
        logo.setPreserveRatio(true);
        Label title = new Label();
        title.textProperty().bind(
                AppSettings.businessNameProperty()
        );
        title.setWrapText(true);
        title.setMaxWidth(150);
        title.getStyleClass().add("sidebar-title");
        title.setTranslateY(1);
        title.setTranslateY(1);

        branding.getChildren().addAll(
                logo,
                title
        );

        Button calendarBtn = createSidebarButton("📅 Calendar");
        Button ledgerBtn = createSidebarButton("📒 A/C Ledger");
        Button accountsBtn = createSidebarButton("👤 Accounts");
        Button payRecBtn = createSidebarButton("💳 Payables & Receivables");
        Button purchaseSalesBtn = createSidebarButton("🛒 Purchases & Sales");
        Button creditNotesBtn = createSidebarButton("🧾 Credit Notes");
        Button trialBalanceBtn = createSidebarButton("⚖ Trial Balance");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button settingsBtn = createSidebarButton("⚙ Settings");

        calendarBtn.setOnAction(e -> {

            setActive(calendarBtn);

            DashboardView.loadCalendarScreen();
        });

        accountsBtn.setOnAction(e -> {

            setActive(accountsBtn);
            DashboardView.loadScreen(AccountView.getView());

        });

        ledgerBtn.setOnAction(e -> {

            setActive(ledgerBtn);
            DashboardView.loadPlaceholderPage("A/C Ledger");

        });

        payRecBtn.setOnAction(e -> {
            setActive(payRecBtn);
            DashboardView.loadPlaceholderPage("Payables & Receivables");
        });

        purchaseSalesBtn.setOnAction(e -> {
            setActive(purchaseSalesBtn);
            DashboardView.loadScreen(PurchaseSalesView.getView());
        });

        creditNotesBtn.setOnAction(e -> {
            setActive(creditNotesBtn);
            DashboardView.loadPlaceholderPage("Credit Notes");
        });

        trialBalanceBtn.setOnAction(e -> {
            setActive(trialBalanceBtn);
            DashboardView.loadPlaceholderPage("Trial Balance");
        });

        settingsBtn.setOnAction(e -> {

            setActive(settingsBtn);

            DashboardView.loadScreen(
                    SettingsView.getView()
            );
        });

        sidebar.getChildren().addAll(
                branding,
                calendarBtn,
                ledgerBtn,
                accountsBtn,
                payRecBtn,
                purchaseSalesBtn,
                creditNotesBtn,
                trialBalanceBtn,
                spacer,
                settingsBtn
        );
        setActive(calendarBtn);

        return sidebar;
    }



    private static Button createSidebarButton(String text) {

        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("sidebar-button");

        return button;
    }

    private static void setActive(Button selectedButton) {

        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-button-active");
        }

        activeButton = selectedButton;

        if (!activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }


}