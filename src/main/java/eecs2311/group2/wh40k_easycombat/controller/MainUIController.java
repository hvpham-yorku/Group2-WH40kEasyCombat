package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.Main;
import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.model.Last_update;
import eecs2311.group2.wh40k_easycombat.repository.Last_updateRepository;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import eecs2311.group2.wh40k_easycombat.service.WarhammerCommunityNewsService;
import javafx.fxml.FXML;
import javafx.concurrent.Task;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainUIController {

    // ======================= Buttons =======================
	@FXML private Button startButton;
    
	@FXML private Button ruleButton;
	
	@FXML private Button armyButton;

    @FXML private Button exitButton;
    @FXML private Button updateButton;

    // ======================= Labels =======================
    @FXML private Label lastUpdateLabel;
    @FXML private Label newsStatusLabel;

    // ======================= Links =======================
    @FXML private Hyperlink wahapediaLink;
    @FXML private Hyperlink moreNewsLink;

    // ======================= Layout =======================
    @FXML private VBox newsLinksBox;

    private static final String WAHAPEDIA_EXPORT_URL =
            "https://wahapedia.ru/wh40k10ed/the-rules/data-export/";
    private static final String WARHAMMER_NEWS_URL =
            "https://www.warhammer-community.com/en-gb/setting/warhammer-40000/";

    // ======================= Setup =======================

    // When this page loads, read the latest update info from the database and show it on the main page.
    @FXML
    private void initialize() {
        loadLastUpdateInfo();
        loadLatestNews();
    }

    // ======================= Main Actions =======================

    // When click "Game Start" button, open the game setup page.
    @FXML
    void startBtn(MouseEvent event) throws IOException {
        FixedAspectView.switchResponsiveTo(
                (Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/GameSetup.fxml",
                1080.0,
                720.0,
                1440.0,
                900.0
        );
    }

    
    // When click "Rules and Datasheets" button, open the datasheets page.
    @FXML
    void ruleBtn(MouseEvent event) throws IOException {
    	FixedAspectView.switchResponsiveTo(
                (Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/Datasheets.fxml",
                1080.0,
                700.0,
                1320.0,
                820.0
        );
    }

    // When click "Army" button, open the army editor page.
    @FXML
    void armyBtn(MouseEvent event) throws IOException {
        FixedAspectView.switchResponsiveTo(
                (Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/Army.fxml",
                1080.0,
                700.0,
                1320.0,
                820.0
        );
    }

    // When click "Exit" button, confirm whether to close the application.
    @FXML
    void exitBtn(MouseEvent event) {
    	Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Unsaved changes will be lost.");
        DialogHelper.styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) exitButton.getScene().getWindow();
            stage.close();
        }
    } 

    // When click "Update" button, show the reserved update action message.
    @FXML
    void updateDataBtn(MouseEvent event) {
        DialogHelper.showInfo("Reserved", "Data update from Wahapedia CSV will be implemented later.");
    }

    // When click "Wahapedia" hyperlink, open the official data export page in the browser.
    @FXML
    void openWahapediaExport() {
        openDocument(WAHAPEDIA_EXPORT_URL);
    }

    // When click "More news..." hyperlink, open the Warhammer 40,000 news page in the browser.
    @FXML
    void openWarhammerNewsPage() {
        openDocument(WARHAMMER_NEWS_URL);
    }

    private void loadLastUpdateInfo() {
        String displayText = "Unknown";

        try {
            List<Last_update> updates = Last_updateRepository.getAllLast_update();
            Last_update latest = updates.stream()
                    .filter(update -> update != null && update.last_update() != null && !update.last_update().isBlank())
                    .max(Comparator.comparingInt(Last_update::auto_id))
                    .orElse(null);

            if (latest != null) {
                displayText = latest.last_update();
            }
        } catch (Exception ignored) {
        }

        if (lastUpdateLabel != null) {
            lastUpdateLabel.setText(displayText);
        }
    }

    private void loadLatestNews() {
        if (newsLinksBox == null || newsStatusLabel == null) {
            return;
        }

        newsLinksBox.getChildren().setAll(newsStatusLabel);
        newsStatusLabel.setText("Loading latest news...");

        Task<List<WarhammerCommunityNewsService.NewsArticle>> newsTask = new Task<>() {
            @Override
            protected List<WarhammerCommunityNewsService.NewsArticle> call() throws Exception {
                return WarhammerCommunityNewsService.fetchLatestNews(15);
            }
        };

        newsTask.setOnSucceeded(event -> populateNews(newsTask.getValue()));
        newsTask.setOnFailed(event -> showNoNetworkMessage());

        Thread loaderThread = new Thread(newsTask, "wh40k-news-loader");
        loaderThread.setDaemon(true);
        loaderThread.start();
    }

    private void populateNews(List<WarhammerCommunityNewsService.NewsArticle> articles) {
        if (newsLinksBox == null) {
            return;
        }

        newsLinksBox.getChildren().clear();

        if (articles == null || articles.isEmpty()) {
            newsStatusLabel.setText("No news available.");
            newsLinksBox.getChildren().add(newsStatusLabel);
            return;
        }

        for (WarhammerCommunityNewsService.NewsArticle article : articles) {
            Hyperlink articleLink = new Hyperlink(buildNewsLinkText(article));
            articleLink.setWrapText(true);
            articleLink.setMaxWidth(Double.MAX_VALUE);
            articleLink.getStyleClass().add("main-news-link");
            articleLink.setOnAction(event -> openArticleLink(article.url()));
            newsLinksBox.getChildren().add(articleLink);
        }
    }

    private void showNoNetworkMessage() {
        if (newsLinksBox == null || newsStatusLabel == null) {
            return;
        }

        newsStatusLabel.setText("No network connection.");
        newsLinksBox.getChildren().setAll(newsStatusLabel);
    }

    private String buildNewsLinkText(WarhammerCommunityNewsService.NewsArticle article) {
        if (article == null) {
            return "";
        }

        if (article.publishedDate() == null || article.publishedDate().isBlank()) {
            return article.title();
        }

        return article.title() + " - " + article.publishedDate();
    }

    private void openArticleLink(String articleUrl) {
        openDocument(articleUrl);
    }

    private void openDocument(String url) {
        try {
            if (Main.getAppHostServices() == null) {
                DialogHelper.showWarning("Open Link Failed", "Desktop browsing is not supported on this machine.");
                return;
            }

            Main.getAppHostServices().showDocument(url);
        } catch (Exception e) {
            DialogHelper.showError("Open Link Error", e);
        }
    }
}
