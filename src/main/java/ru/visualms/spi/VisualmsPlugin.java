package ru.visualms.spi;

import java.util.List;

/**
 * A visualms plugin: a jar in the {@code plugins} folder of the server that names its implementation in
 * {@code META-INF/services/ru.visualms.spi.VisualmsPlugin} and the SPI it was built against in its manifest
 * ({@link Spi#MANIFEST_ATTRIBUTE}). It is loaded once on start; a plugin that fails to load or needs another SPI
 * ({@link Spi#compatible(String)}) is skipped and listed in the admin panel with the reason.
 * <p>
 * A plugin sees only this SPI: it computes from {@link ReportData} and never touches the database, the tables or
 * the classes of the core.
 */
public interface VisualmsPlugin {

    /** Unique, stable, lower case: {@code acme-retail}; prefixes the codes of its metrics ({@code acme-retail.foo}). */
    String id();

    String title();

    String version();

    /** Who made it, e.g. «ООО Акме»; the core's own plugins say «visualms». */
    String vendor();

    default List<MetricSource> metricSources() {
        return List.of();
    }

    default List<MetricCalculator> calculators() {
        return List.of();
    }

    default List<WidgetType> widgetTypes() {
        return List.of();
    }

    /**
     * Ready reports of the plugin's metrics: each is made once as a draft for the report editors (see
     * {@link ReportTemplate}). Since 1.3
     */
    default List<ReportTemplate> reportTemplates() {
        return List.of();
    }
}
