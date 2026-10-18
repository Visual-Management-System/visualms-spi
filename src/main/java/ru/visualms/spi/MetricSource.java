package ru.visualms.spi;

import java.util.List;

/**
 * Contributes metrics to the catalog. The core registers the built-in ones; a plugin adds its own with a
 * {@link MetricCalculator} for them. Codes must be unique across sources: a plugin prefixes them with its id.
 */
public interface MetricSource {

    List<MetricDescriptor> metrics();
}
