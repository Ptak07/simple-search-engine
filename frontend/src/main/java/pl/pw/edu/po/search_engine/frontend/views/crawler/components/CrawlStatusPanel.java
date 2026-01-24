package pl.pw.edu.po.search_engine.frontend.views.crawler.components;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import pl.pw.edu.po.search_engine.frontend.model.CrawlHistoryDTO;
import pl.pw.edu.po.search_engine.frontend.services.api.CrawlerApiClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Real-time crawl status panel component.
 * Shows progress, statistics, and auto-refreshes every 2 seconds.
 */
public class CrawlStatusPanel extends VerticalLayout {

    private final CrawlerApiClient apiClient;
    private final Long crawlId;
    private final int maxPages;

    // UI Components
    private final Span statusBadge;
    private final ProgressBar progressBar;
    private final Span progressLabel;
    private final Span pagesLabel;
    private final Span indexedLabel;
    private final Span durationLabel;
    private final Span errorLabel;
    private final Button refreshButton;
    private final Button cancelButton;

    // Scheduled update
    private ScheduledExecutorService scheduler;
    private CrawlHistoryDTO currentStatus;

    public CrawlStatusPanel(Long crawlId, int maxPages, CrawlerApiClient apiClient) {
        this.crawlId = crawlId;
        this.maxPages = maxPages;
        this.apiClient = apiClient;

        // Panel styling
        setPadding(true);
        setSpacing(true);
        setWidthFull();
        getStyle()
            .set("border", "1px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("background-color", "var(--lumo-base-color)");

        // Title
        H3 title = new H3("Crawl Progress");
        title.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-s)")
            .set("font-size", "var(--lumo-font-size-l)");

        // Status badge - minimal
        statusBadge = new Span("Initializing...");
        statusBadge.getElement().getThemeList().add("badge");
        statusBadge.getElement().getThemeList().add("primary");
        statusBadge.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        // Progress bar with percentage
        progressBar = new ProgressBar(0, 100);
        progressBar.setWidthFull();
        progressBar.setValue(0);
        progressBar.getStyle()
            .set("height", "24px")
            .set("margin-bottom", "var(--lumo-space-xs)");

        progressLabel = new Span("0%");
        progressLabel.getStyle()
            .set("font-weight", "500")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "var(--lumo-font-size-s)");

        // Statistics - clean layout
        pagesLabel = new Span("Pages: 0/" + maxPages);
        indexedLabel = new Span("Indexed: 0");
        durationLabel = new Span("Duration: --:--:--");

        pagesLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");
        indexedLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");
        durationLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");

        errorLabel = new Span();
        errorLabel.getStyle()
            .set("color", "var(--lumo-error-text-color)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("margin-top", "var(--lumo-space-xs)");
        errorLabel.setVisible(false);

        HorizontalLayout statsLayout = new HorizontalLayout(pagesLabel, indexedLabel, durationLabel);
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        statsLayout.getStyle().set("margin-top", "var(--lumo-space-xs)");

        // Action buttons - subtle
        refreshButton = new Button("Refresh", e -> updateStatus());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        cancelButton = new Button("Cancel", e -> cancelCrawl());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(refreshButton, cancelButton);
        buttonLayout.setSpacing(true);
        buttonLayout.getStyle().set("margin-top", "var(--lumo-space-s)");

        // Layout
        add(
            title,
            statusBadge,
            progressBar,
            progressLabel,
            statsLayout,
            errorLabel,
            buttonLayout
        );

        // Start auto-refresh
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            UI ui = getUI().orElse(null);
            if (ui != null) {
                ui.access(this::updateStatus);
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void updateStatus() {
        try {
            currentStatus = apiClient.getCrawlStatus(crawlId);

            if (currentStatus != null) {
                // Update status badge - clean text
                String statusText = switch (currentStatus.getStatus()) {
                    case "STARTED" -> "In Progress";
                    case "SUCCESS" -> "Completed";
                    case "FAILED" -> "Failed";
                    case "PARTIAL" -> "Partial Success";
                    case "CANCELLED" -> "Cancelled";
                    default -> currentStatus.getStatus();
                };
                statusBadge.setText(statusText);
                updateBadgeColor(currentStatus.getStatus());

                // Update progress bar
                double progress = currentStatus.getProgress(maxPages);
                progressBar.setValue(progress);
                progressLabel.setText(String.format("%.0f%%", progress));

                // Update statistics - no emojis
                pagesLabel.setText("Pages: " + currentStatus.getPagesCrawled() + "/" + maxPages);
                indexedLabel.setText("Indexed: " + currentStatus.getDocumentsIndexed());
                durationLabel.setText("Duration: " + currentStatus.getFormattedDuration());

                // Show error message if exists
                if (currentStatus.getErrorMessage() != null && !currentStatus.getErrorMessage().isEmpty()) {
                    errorLabel.setText("Error: " + currentStatus.getErrorMessage());
                    errorLabel.setVisible(true);
                } else {
                    errorLabel.setVisible(false);
                }

                // Stop auto-refresh if crawl finished
                if (!currentStatus.isActive()) {
                    stopAutoRefresh();
                    refreshButton.setEnabled(false);
                    refreshButton.setText("Finished");
                    cancelButton.setEnabled(false);
                    cancelButton.setVisible(false);
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to update crawl status: " + e.getMessage());
            errorLabel.setText("Failed to fetch status: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    private void cancelCrawl() {
        try {
            boolean cancelled = apiClient.cancelCrawl(crawlId);

            if (cancelled) {
                statusBadge.setText("Cancelling...");
                statusBadge.getElement().getThemeList().clear();
                statusBadge.getElement().getThemeList().add("badge");
                statusBadge.getElement().getThemeList().add("contrast");

                cancelButton.setEnabled(false);
                cancelButton.setText("Cancelled");

                Notification.show("Crawl cancellation requested", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Force immediate status update
                updateStatus();
            } else {
                Notification.show("Failed to cancel crawl - it may have already finished",
                    3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

        } catch (Exception e) {
            System.err.println("Failed to cancel crawl: " + e.getMessage());
            Notification.show("Error cancelling crawl: " + e.getMessage(),
                5000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateBadgeColor(String status) {
        statusBadge.getElement().getThemeList().clear();
        statusBadge.getElement().getThemeList().add("badge");

        switch (status) {
            case "STARTED" -> statusBadge.getElement().getThemeList().add("primary");
            case "SUCCESS" -> statusBadge.getElement().getThemeList().add("success");
            case "FAILED" -> statusBadge.getElement().getThemeList().add("error");
            case "PARTIAL" -> statusBadge.getElement().getThemeList().add("contrast");
            default -> statusBadge.getElement().getThemeList().add("primary");
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        stopAutoRefresh();
        super.onDetach(detachEvent);
    }

    private void stopAutoRefresh() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Get current crawl status.
     */
    public CrawlHistoryDTO getCurrentStatus() {
        return currentStatus;
    }
}

