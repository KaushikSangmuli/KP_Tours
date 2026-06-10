package KP_TOURS.ui.sidebar;

import KP_TOURS.ui.accountledger.AccountLedgerView;
import KP_TOURS.ui.accounts.AccountView;
import KP_TOURS.ui.dashboard.DashboardView;
import KP_TOURS.ui.paymentsreceive.PayReceiveView;
import KP_TOURS.ui.purchasesales.PurchaseSalesView;
import KP_TOURS.ui.settings.AppSettings;
import KP_TOURS.ui.settings.SettingsView;
import KP_TOURS.util.SvgIconUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
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

        logo.setFitWidth(70);
        logo.setFitHeight(90);
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

        Button calendarBtn = createSidebarButton(
                "Calendar",
                SvgIconUtil.loadIcon("/icons/calendar.png", 18)
        );

        Button ledgerBtn = createSidebarButton(
                "A/C Ledger",
                SvgIconUtil.loadIcon("/icons/ledger.png", 18)
        );

        Button accountsBtn = createSidebarButton(
                "Accounts",
                SvgIconUtil.loadIcon("/icons/accounts.png", 18)
        );

        Button payRecBtn = createSidebarButton(
                "Payables & Receivables",
                SvgIconUtil.loadIcon("/icons/payables.png", 18)
        );

        Button purchaseSalesBtn = createSidebarButton(
                "Purchases & Sales",
                SvgIconUtil.loadIcon("/icons/purchase.png", 18)
        );

        Button creditNotesBtn = createSidebarButton(
                "Credit Notes",
                SvgIconUtil.loadIcon("/icons/note.png", 18)
        );

        Button trialBalanceBtn = createSidebarButton(
                "Trial Balance",
                SvgIconUtil.loadIcon("/icons/trialbalance.png", 18)
        );

        Button settingsBtn = createSidebarButton(
                "Settings",
                SvgIconUtil.loadIcon("/icons/settings.png", 18)
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);


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
            DashboardView.loadScreen(
                    AccountLedgerView.getView()
            );
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

        payRecBtn.setOnAction(e -> {
            setActive(payRecBtn);
            DashboardView.loadScreen(PayReceiveView.getView());
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


    private static Button createSidebarButton(String text, Node icon) {

        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().addAll(icon, label);

        Button btn = new Button();
        btn.setGraphic(content);

        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 10 15;");

        return btn;
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