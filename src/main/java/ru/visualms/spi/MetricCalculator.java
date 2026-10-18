package ru.visualms.spi;

import java.util.Set;

/**
 * Computes the metrics a plugin adds to the catalog ({@link MetricSource}). It reads only {@link ReportData}: the
 * scope of the viewer, already narrowed by their rights and by what they chose on the report.
 * <p>
 * Called per request on a server thread; must not keep state between calls. A thrown exception hides the widget
 * («не удалось посчитать»), the report goes on.
 */
public interface MetricCalculator {

    /** The codes of the metrics this calculator computes. */
    Set<String> codes();

    /**
     * @param query what the viewer chose: the widget's months, a span, a day, a measure, filters (only those the
     *              metric declares; the data are already narrowed by the filters and dated by the day)
     */
    MetricResult compute(String code, ReportData data, MetricQuery query);
}
