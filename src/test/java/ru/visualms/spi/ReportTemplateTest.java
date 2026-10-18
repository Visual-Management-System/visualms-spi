package ru.visualms.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import ru.visualms.spi.MetricDescriptor.Scope;
import ru.visualms.spi.MetricDescriptor.Visualization;
import ru.visualms.spi.ReportTemplate.Height;
import ru.visualms.spi.ReportTemplate.Widget;

class ReportTemplateTest {

    @Test
    void aWidgetTakesTheGridsWidthsAndAPeriodOfUpToFiveYears() {
        Widget w = Widget.of("acme.rent", 4).as(Visualization.KPI).months(12).tall();
        assertEquals(4, w.width());
        assertEquals(Visualization.KPI, w.visualization());
        assertEquals(12, w.months());
        assertEquals(Height.L, w.height());
        assertEquals(Height.M, new Widget("acme.rent", null, null, 6, null, null).height(), "the height defaults to M");
        assertThrows(IllegalArgumentException.class, () -> Widget.of("acme.rent", 5));
        assertThrows(IllegalArgumentException.class, () -> Widget.of("acme.rent", 4).months(61));
        assertThrows(NullPointerException.class, () -> Widget.of(null, 4));
    }

    @Test
    void aTemplateHasACodeATitleAScopeAndAVersionFromOne() {
        ReportTemplate t = new ReportTemplate("income", "Доходы", null, 1, Scope.PORTFOLIO, List.of(Widget.of("acme.rent", 12)));
        assertEquals(1, t.widgets().size());
        assertThrows(UnsupportedOperationException.class, () -> t.widgets().add(Widget.of("acme.rent", 12)));
        assertThrows(IllegalArgumentException.class,
                () -> new ReportTemplate("income", "Доходы", null, 0, Scope.PORTFOLIO, List.of()));
        assertThrows(NullPointerException.class,
                () -> new ReportTemplate("income", "Доходы", null, 1, null, List.of()));
    }

    @Test
    void aPluginHasNoTemplatesUnlessItSaysSo() {
        VisualmsPlugin plain = new VisualmsPlugin() {
            public String id() { return "plain"; }
            public String title() { return "Plain"; }
            public String version() { return "1.0.0"; }
            public String vendor() { return "test"; }
        };
        assertTrue(plain.reportTemplates().isEmpty());
    }
}
