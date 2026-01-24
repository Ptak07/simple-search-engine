package pl.pw.edu.po.search_engine.frontend.views.search;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pl.pw.edu.po.search_engine.frontend.model.SearchResponseDTO;
import pl.pw.edu.po.search_engine.frontend.model.SearchResultDTO;
import pl.pw.edu.po.search_engine.frontend.services.api.SearchApiClient;
import pl.pw.edu.po.search_engine.frontend.views.MainLayout;

/**
 * Search view with query input and results display.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Search | Search Engine")
public class SearchView extends VerticalLayout {

    private final SearchApiClient searchApiClient;
    private final TextField searchField;
    private final VerticalLayout resultsContainer;
    private final Paragraph statsLabel;

    public SearchView(SearchApiClient searchApiClient) {
        this.searchApiClient = searchApiClient;

        setSpacing(false);
        setPadding(true);
        setMaxWidth("1200px");
        setWidthFull();
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Search Engine");
        title.getStyle().set("margin-top", "0");

        searchField = new TextField();
        searchField.setPlaceholder("Enter search query...");
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);

        Button searchButton = new Button("Search");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> performSearch());

        searchField.addKeyPressListener(event -> {
            if (event.getKey().getKeys().get(0).equals("Enter")) {
                performSearch();
            }
        });

        HorizontalLayout searchBar = new HorizontalLayout(searchField, searchButton);
        searchBar.setWidthFull();
        searchBar.setFlexGrow(1, searchField);
        searchBar.setAlignItems(Alignment.END);

        statsLabel = new Paragraph();
        statsLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        resultsContainer = new VerticalLayout();
        resultsContainer.setSpacing(true);
        resultsContainer.setPadding(false);
        resultsContainer.setWidthFull();

        add(title, searchBar, statsLabel, resultsContainer);
    }

    private void performSearch() {
        String query = searchField.getValue();

        if (query == null || query.trim().isEmpty()) {
            Notification.show("Please enter a search query", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        System.out.println("Performing search: " + query);
        resultsContainer.removeAll();
        statsLabel.setText("Searching...");

        try {
            SearchResponseDTO response = searchApiClient.simpleSearch(query.trim(), "pl");

            if (response.getResults() == null || response.getResults().isEmpty()) {
                statsLabel.setText("No results found");
                Paragraph noResults = new Paragraph("No documents match your query. Try different keywords.");
                noResults.getStyle().set("color", "var(--lumo-secondary-text-color)");
                resultsContainer.add(noResults);
                return;
            }

            statsLabel.setText(String.format("Found %d results in %d ms",
                    response.getTotalResults(),
                    response.getSearchTimeMs()));

            for (SearchResultDTO result : response.getResults()) {
                resultsContainer.add(createResultCard(result));
            }

            Notification.show("Search completed successfully", 2000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage());
            statsLabel.setText("Search failed");
            Notification.show("Search failed: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private Div createResultCard(SearchResultDTO result) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "var(--lumo-space-m)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-base-color)")
                .set("margin-bottom", "var(--lumo-space-s)");

        Anchor titleLink = new Anchor(result.getDocument().getUrl(), result.getDocument().getTitle());
        titleLink.setTarget("_blank");
        titleLink.getStyle()
                .set("display", "block")
                .set("margin", "0 0 var(--lumo-space-xs) 0")
                .set("font-size", "20px")
                .set("font-weight", "400")
                .set("color", "#1a0dab")
                .set("text-decoration", "none")
                .set("cursor", "pointer");

        titleLink.getElement().addEventListener("mouseenter", e ->
            titleLink.getStyle().set("text-decoration", "underline")
        );
        titleLink.getElement().addEventListener("mouseleave", e ->
            titleLink.getStyle().set("text-decoration", "none")
        );

        Anchor urlLink = new Anchor(result.getDocument().getUrl(), result.getDocument().getUrl());
        urlLink.setTarget("_blank");
        urlLink.getStyle()
                .set("display", "block")
                .set("margin", "0 0 var(--lumo-space-xs) 0")
                .set("font-size", "14px")
                .set("color", "#006621")
                .set("text-decoration", "none");

        Paragraph snippet = new Paragraph(result.getSnippet() != null ? result.getSnippet() : "");
        snippet.getStyle()
                .set("margin", "0 0 var(--lumo-space-xs) 0")
                .set("color", "var(--lumo-secondary-text-color)");

        Paragraph score = new Paragraph(String.format("Score: %.2f", result.getScore()));
        score.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-tertiary-text-color)");

        card.add(titleLink, urlLink, snippet, score);
        return card;
    }
}
