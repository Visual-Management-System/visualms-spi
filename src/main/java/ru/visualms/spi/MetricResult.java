package ru.visualms.spi;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * A computed metric, ready to draw. One of three kinds:
 * <ul>
 *   <li>{@code VALUE} — {@link #value} with {@link #note} (e.g. «12 из 40»);</li>
 *   <li>{@code SERIES} — {@link #categories} (months or labels) and one or more {@link #series};</li>
 *   <li>{@code TABLE} — {@link #columns} and {@link #rows} (values by column key).</li>
 * </ul>
 * {@link #hidden} is set instead when the viewer may not see it (the reason), {@link #empty} when there is no data.
 * A series may carry {@link #details}: the rows behind each category (a click on a column lists them).
 */
public record MetricResult(Kind kind, BigDecimal value, String unit, String note, List<String> categories,
                           List<Series> series, List<Column> columns, List<Map<String, Object>> rows, String hidden,
                           boolean empty, List<Column> detailColumns, Map<String, List<Map<String, Object>>> details) {

    public enum Kind { VALUE, SERIES, TABLE }

    /** @param unit «м²», «₽»… — a series may differ from the metric (expiry profile: area and income) */
    public record Series(String name, String unit, List<BigDecimal> data) {
    }

    /**
     * @param type text, number, money, area, percent, date, lease (value: {id, number}), status
     */
    public record Column(String key, String title, String type) {
    }

    public static MetricResult value(BigDecimal value, String unit, String note) {
        return new MetricResult(Kind.VALUE, value, unit, note, null, null, null, null, null, value == null, null, null);
    }

    public static MetricResult series(String unit, List<String> categories, List<Series> series) {
        boolean empty = categories.isEmpty() || series.stream().allMatch(s -> s.data().stream()
                .allMatch(v -> v == null || v.signum() == 0));
        return new MetricResult(Kind.SERIES, null, unit, null, categories, series, null, null, null, empty, null, null);
    }

    public static MetricResult table(List<Column> columns, List<Map<String, Object>> rows) {
        return new MetricResult(Kind.TABLE, null, null, null, null, null, columns, rows, null, rows.isEmpty(), null, null);
    }

    public static MetricResult hidden(String reason) {
        return new MetricResult(null, null, null, null, null, null, null, null, reason, false, null, null);
    }

    /** The rows behind each category: category → rows with {@code columns}. */
    public MetricResult withDetails(List<Column> columns, Map<String, List<Map<String, Object>>> details) {
        return new MetricResult(kind, value, unit, note, categories, series, this.columns, rows, hidden, empty, columns,
                details);
    }
}
