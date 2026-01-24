package pl.pw.edu.po.search_engine.frontend.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import pl.pw.edu.po.search_engine.frontend.views.crawler.CrawlerView;
import pl.pw.edu.po.search_engine.frontend.views.search.SearchView;

/**
 * Main layout with navigation drawer.
 * Used as parent layout for all views.
 */
@PageTitle("Search Engine Dashboard")
public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 appName = new H1("Search Engine");
        appName.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE
        );

        DrawerToggle toggle = new DrawerToggle();

        HorizontalLayout header = new HorizontalLayout(toggle, appName);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM
        );

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        SideNavItem searchItem = new SideNavItem("Search", SearchView.class, VaadinIcon.SEARCH.create());

        SideNavItem crawlerItem = new SideNavItem("Crawler", CrawlerView.class, VaadinIcon.DOWNLOAD.create());

        nav.addItem(searchItem);
        nav.addItem(crawlerItem);

        addToDrawer(nav);
    }
}
