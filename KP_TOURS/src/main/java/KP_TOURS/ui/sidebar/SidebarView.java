package KP_TOURS.ui.sidebar;

import KP_TOURS.ui.accountledger.AccountLedgerView;
import KP_TOURS.ui.accounts.AccountView;
import KP_TOURS.ui.creditnotes.CreditNotesView;
import KP_TOURS.ui.dashboard.DashboardView;
import KP_TOURS.ui.paymentsreceive.PayReceiveView;
import KP_TOURS.ui.purchasesales.PurchaseSalesView;
import KP_TOURS.ui.settings.AppSettings;
import KP_TOURS.ui.settings.SettingsView;
import KP_TOURS.util.SvgIconUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static KP_TOURS.ui.purchasesales.PurchaseSalesView.requestFocusSafely;

public class SidebarView {

    private static Button activeButton;
    // Add this static list to track sidebar buttons in order
    private static final List<Button> navButtons = new ArrayList<>();
    // Add this static flag
    private static boolean sidebarActive = false;

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

        // ADD this after all buttons are created
        navButtons.clear();
        navButtons.addAll(List.of(
                calendarBtn,
                ledgerBtn,
                accountsBtn,
                payRecBtn,
                purchaseSalesBtn,
                creditNotesBtn,
                trialBalanceBtn,
                settingsBtn
        ));

        // ADD this loop after navButtons.addAll(...)
        for (Button btn : navButtons) {
            btn.setFocusTraversable(true);

            // Visual highlight on focus
            btn.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (isFocused) {
                    btn.getStyleClass().add("sidebar-button-focused");
                } else {
                    btn.getStyleClass().remove("sidebar-button-focused");
                }
            });

            btn.setOnKeyPressed(e -> {

                if (!sidebarActive) return;

                switch (e.getCode()) {

                    case ENTER -> {
                        e.consume();
                        sidebarActive = false;
                        btn.fire();

                        Platform.runLater(() -> {
                            javafx.scene.Node targetNode = getTargetNode();
                            if (targetNode == null) return;

                            if (targetNode.getScene() != null) {
                                // Already in scene, focus immediately
                                targetNode.requestFocus();
                            } else {
                                // Wait until it's actually attached to the scene
                                targetNode.sceneProperty().addListener(new javafx.beans.value.ChangeListener<>() {
                                    @Override
                                    public void changed(javafx.beans.value.ObservableValue<? extends javafx.scene.Scene> obs,
                                                        javafx.scene.Scene oldScene,
                                                        javafx.scene.Scene newScene) {
                                        if (newScene != null) {
                                            obs.removeListener(this); // remove after first trigger
                                            Platform.runLater(targetNode::requestFocus);
                                        }
                                    }
                                });
                            }
                        });
                    }

                    case UP -> {
                        e.consume();
                        int i = navButtons.indexOf(btn);
                        if (i > 0) navButtons.get(i - 1).requestFocus();
                    }

                    case DOWN -> {
                        e.consume();
                        int i = navButtons.indexOf(btn);
                        if (i < navButtons.size() - 1) navButtons.get(i + 1).requestFocus();
                    }

                    case ESCAPE -> {
                        e.consume();
                        sidebarActive = false;
                        btn.getStyleClass().remove("sidebar-button-focused");
                        Platform.runLater(() -> returnFocusToScreen());
                    }
                }
            });
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        calendarBtn.setOnAction(e -> {
            setActive(calendarBtn);
            DashboardView.loadCalendarScreen();
            Platform.runLater(() ->
                    Platform.runLater(() -> DashboardView.calendarRoot.requestFocus())
            );
        });

        accountsBtn.setOnAction(e -> {
            setActive(accountsBtn);
            DashboardView.loadScreen(AccountView.getView());
            Platform.runLater(() ->
                    Platform.runLater(() -> AccountView.getFocusTarget().requestFocus())
            );
        });

        ledgerBtn.setOnAction(e -> {
            setActive(ledgerBtn);
            DashboardView.loadScreen(AccountLedgerView.getView());
            Platform.runLater(() ->
                    Platform.runLater(() -> AccountLedgerView.getFocusTarget().requestFocus())
            );
        });

        purchaseSalesBtn.setOnAction(e -> {
            setActive(purchaseSalesBtn);
            DashboardView.loadScreen(PurchaseSalesView.getView());
            requestFocusSafely(PurchaseSalesView.getFocusTarget());
        });

        payRecBtn.setOnAction(e -> {
            setActive(payRecBtn);
            DashboardView.loadScreen(PayReceiveView.getView());
//            Platform.runLater(() ->
//                    Platform.runLater(() -> PayReceiveView.getFocusTarget().requestFocus())
//            );
        });

// these have no focus target, keep as is
        creditNotesBtn.setOnAction(e -> {
            setActive(creditNotesBtn);
            DashboardView.loadScreen(CreditNotesView.getView());
            Platform.runLater(() ->
                    Platform.runLater(() -> CreditNotesView.getFocusTarget().requestFocus())
            );
        });

        trialBalanceBtn.setOnAction(e -> {
            setActive(trialBalanceBtn);
            DashboardView.loadPlaceholderPage("Trial Balance");
        });

        settingsBtn.setOnAction(e -> {
            setActive(settingsBtn);
            DashboardView.loadScreen(SettingsView.getView());
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


    public static void attachShortcuts(javafx.scene.Scene scene) {

        scene.addEventFilter(
                javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {

                    if (e.isControlDown() && e.getCode() == javafx.scene.input.KeyCode.T) {
                        e.consume();

                        if (sidebarActive) {
                            // Ctrl+T again → exit sidebar, return to screen
                            sidebarActive = false;
                            returnFocusToScreen();
                            return;
                        }

                        sidebarActive = true;
                        Button toFocus = activeButton != null ? activeButton : navButtons.get(0);
                        Platform.runLater(toFocus::requestFocus);
                    }
                });
    }

    private static void returnFocusToScreen() {

        if (activeButton == null) return;

        javafx.scene.Node target = getTargetNode();
        if (target == null) return;

        if (target.getScene() != null) {
            Platform.runLater(target::requestFocus);
        } else {
            target.sceneProperty().addListener(new javafx.beans.value.ChangeListener<>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends javafx.scene.Scene> obs,
                                    javafx.scene.Scene oldScene,
                                    javafx.scene.Scene newScene) {
                    if (newScene != null) {
                        obs.removeListener(this);
                        Platform.runLater(target::requestFocus);
                    }
                }
            });
        }
    }

    private static String getButtonLabel(Button btn) {
        if (btn.getGraphic() instanceof HBox hbox) {
            for (var node : hbox.getChildren()) {
                if (node instanceof Label lbl) return lbl.getText();
            }
        }
        return "";
    }

    private static javafx.scene.Node getTargetNode() {

        if (activeButton == null) return null;

        String label = getButtonLabel(activeButton);

        javafx.scene.Node target = switch (label) {
            case "Calendar"               -> DashboardView.calendarRoot;
            case "Accounts"               -> AccountView.getFocusTarget();
            case "A/C Ledger"             -> AccountLedgerView.getFocusTarget();
//            case "Payables & Receivables" -> PayReceiveView.getFocusTarget();
            case "Purchases & Sales"      -> PurchaseSalesView.getFocusTarget();
            default                       -> null;
        };

        return target;
    }

}