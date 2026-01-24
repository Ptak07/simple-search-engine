package pl.pw.edu.po.search_engine.frontend.views.crawler;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pl.pw.edu.po.search_engine.frontend.services.api.CrawlerApiClient;
import pl.pw.edu.po.search_engine.frontend.views.MainLayout;

/**
 * Simple crawler control panel.
 * Starts crawl and shows message to check history.
 */
@Route(value = "crawler", layout = MainLayout.class)
@PageTitle("Crawler | Search Engine")
public class CrawlerView extends VerticalLayout {

    private final CrawlerApiClient crawlerApiClient;

    private final TextField urlField;
    private final IntegerField maxDepthField;
    private final IntegerField maxPagesField;
    private final Button startButton;
    private final Paragraph statusLabel;

    public CrawlerView(CrawlerApiClient crawlerApiClient) {
        this.crawlerApiClient = crawlerApiClient;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("800px");
        setWidthFull();
        getStyle().set("margin", "0 auto");

        H2 title = new H2("🕷️ Web Crawler");
        Paragraph description = new Paragraph(
            "Start crawling a website to index its content. The crawler will run in the background."
        );

        urlField = new TextField("Root URL");
        urlField.setPlaceholder("https://example.com");
        urlField.setWidthFull();
        urlField.setValue("https://en.wikipedia.org/wiki/Java_(programming_language)");

        maxDepthField = new IntegerField("Max Depth");
        maxDepthField.setValue(2);
        maxDepthField.setMin(1);
        maxDepthField.setMax(5);
        maxDepthField.setStepButtonsVisible(true);
        maxDepthField.setHelperText("How many levels deep to crawl");

        maxPagesField = new IntegerField("Max Pages");
        maxPagesField.setValue(50);
        maxPagesField.setMin(10);
        maxPagesField.setMax(1000);
        maxPagesField.setStepButtonsVisible(true);
        maxPagesField.setHelperText("Maximum number of pages to index");

        HorizontalLayout configRow = new HorizontalLayout(maxDepthField, maxPagesField);
        configRow.setWidthFull();

        startButton = new Button("🚀 Start Crawling");
        startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        startButton.addClickListener(e -> startCrawl());

        statusLabel = new Paragraph("Ready to start crawling");
        statusLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout infoBox = new VerticalLayout();
        infoBox.setPadding(true);
        infoBox.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)");

        H3 infoTitle = new H3("ℹ️ How it works");
        Paragraph info1 = new Paragraph("1. Enter the URL you want to crawl");
        Paragraph info2 = new Paragraph("2. Set maximum depth and pages");
        Paragraph info3 = new Paragraph("3. Click 'Start Crawling' - the process runs in background");
        Paragraph info4 = new Paragraph("4. Check indexed documents in the Search page");

        infoBox.add(infoTitle, info1, info2, info3, info4);

        add(
            title,
            description,
            urlField,
            configRow,
            startButton,
            statusLabel,
            infoBox
        );
    }

    private void startCrawl() {
        String url = urlField.getValue();

        if (url == null || url.trim().isEmpty()) {
            Notification.show("Please enter a valid URL", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        System.out.println("Starting crawl: " + url);
        statusLabel.setText("Starting crawl...");
        startButton.setEnabled(false);

        try {
            crawlerApiClient.startCrawl(url, maxDepthField.getValue(), maxPagesField.getValue());

            statusLabel.setText("✅ Crawl started successfully! The crawler is running in the background.");
            statusLabel.getStyle().set("color", "var(--lumo-success-text-color)");

            Notification notification = Notification.show(
                "Crawling started! Check the Search page in a few moments to see indexed content.",
                5000,
                Notification.Position.BOTTOM_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            System.err.println("Failed to start crawl: " + e.getMessage());
            statusLabel.setText("❌ Failed to start crawl: " + e.getMessage());
            statusLabel.getStyle().set("color", "var(--lumo-error-text-color)");

            Notification.show("Failed to start crawl", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } finally {
            startButton.setEnabled(true);
        }
    }
}
