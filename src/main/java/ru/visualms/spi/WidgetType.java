package ru.visualms.spi;

import java.util.Set;

/**
 * A way to draw a metric that a plugin brings: a static package (JS, CSS, images) inside the plugin's jar. It runs in
 * an isolated iframe — no access to the page, the token or the network; it gets the computed {@link MetricResult}
 * and draws it.
 * <p>
 * The package's entry is a script that defines {@code render(data, el, lib)}: {@code data} — the result,
 * {@code el} — the element to draw in, {@code lib} — {@code echarts}, {@code format} and {@code drill(rows)} (opens
 * the rows behind a click in the host page).
 *
 * @param code   unique among the plugin's widget types
 * @param shapes the results it can draw
 * @param entry  the path of the entry script inside the jar, e.g. {@code widgets/gauge/index.js}; the other files
 *               of the package lie next to it
 */
public record WidgetType(String code, String title, String description, Set<MetricDescriptor.Shape> shapes,
                         String entry) {
}
