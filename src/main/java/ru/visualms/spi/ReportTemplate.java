package ru.visualms.spi;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A ready report (dashboard) a plugin ships with its metrics. The core makes a report of it once, the first time the
 * plugin is loaded: a draft seen by the report editors (ADMIN, MASTER_MANAGER), who publish it to whom they choose.
 * Later starts apply a newer {@link #version()} to a report nobody has changed; a report edited by a person stays as
 * it is, a deleted (archived) one is not made again. A template naming a metric the catalog lacks is skipped with the
 * reason in the admin panel. Since 1.3
 *
 * @param code        stable and unique within the plugin, lower case: {@code income}; the report is bound to it
 * @param title       the report's title, as the user sees it
 * @param description shown under the title; null — none
 * @param version     raise it when the widgets change: untouched reports made of an older version are rebuilt
 * @param scope       one building chosen when the report is opened, or the viewer's portfolio
 * @param widgets     in their order on the report (rows fill up to 12 columns)
 */
public record ReportTemplate(String code, String title, String description, int version, MetricDescriptor.Scope scope,
                             List<Widget> widgets) {

    /** The widths a widget may take, in columns of 12. */
    public static final Set<Integer> WIDTHS = Set.of(3, 4, 6, 8, 12);

    public ReportTemplate {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(scope, "scope");
        widgets = List.copyOf(widgets);
        if (version < 1) {
            throw new IllegalArgumentException("version starts at 1");
        }
    }

    /** The height of a widget on the report. */
    public enum Height { S, M, L }

    /**
     * A widget: a metric of the catalog shown one way. The viewer's filters and day apply as on any report.
     *
     * @param metric        the metric's full code, e.g. {@code acme-retail.rent_share} (a plugin's own or the core's)
     * @param visualization null — the metric's default; must be one the metric allows
     * @param title         null — the metric's title
     * @param width         3, 4, 6, 8 or 12 columns
     * @param months        the period of a metric with one, 1–60; null — the metric's default
     */
    public record Widget(String metric, MetricDescriptor.Visualization visualization, String title, int width,
                         Height height, Integer months) {

        public Widget {
            Objects.requireNonNull(metric, "metric");
            if (!WIDTHS.contains(width)) {
                throw new IllegalArgumentException("width is 3, 4, 6, 8 or 12");
            }
            if (months != null && (months < 1 || months > 60)) {
                throw new IllegalArgumentException("months is 1–60");
            }
            height = height == null ? Height.M : height;
        }

        /** A metric in its default visualization and period. */
        public static Widget of(String metric, int width) {
            return new Widget(metric, null, null, width, Height.M, null);
        }

        public Widget as(MetricDescriptor.Visualization v) {
            return new Widget(metric, v, title, width, height, months);
        }

        public Widget tall() {
            return new Widget(metric, visualization, title, width, Height.L, months);
        }

        public Widget months(int n) {
            return new Widget(metric, visualization, title, width, height, n);
        }
    }
}
