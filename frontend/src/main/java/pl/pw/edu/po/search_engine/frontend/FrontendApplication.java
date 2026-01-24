package pl.pw.edu.po.search_engine.frontend;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Search Engine Frontend.
 *
 * Features:
 * - @Push enables server-push for real-time updates (WebSocket/Server-Sent Events)
 * - @Theme applies Vaadin's Lumo theme
 * - @PWA makes the app installable as Progressive Web App
 */
@SpringBootApplication
@Push
@PWA(
    name = "Search Engine Dashboard",
    shortName = "SE Dashboard",
    description = "Full-text search engine with crawler and analytics"
)
public class FrontendApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }
}
