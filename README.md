# visualms-spi

Contracts for visualms plugins: report metrics, raw values, report data with the viewer's permissions, widget types.
No dependencies, Java 17. A third-party developer builds a plugin against this library and sees neither the database
nor the core classes.

## Plugin

- the jar goes into the server's `plugins` folder;
- `META-INF/services/ru.visualms.spi.VisualmsPlugin` — the name of the class implementing `VisualmsPlugin`;
- the jar manifest states the SPI version it was built against: `Visualms-Spi-Version: 1.0.0`;
- metrics — `MetricSource` (descriptions) + `MetricCalculator` (calculation from `ReportData`), codes prefixed with
  the plugin id;
- widgets — `WidgetType`: a static package inside the jar, rendered in an isolated iframe by the function
  `render(data, el, lib)`;
- ready reports — `ReportTemplate` (since 1.3): the core makes each a draft report for the report editors once, applies
  a newer template version to a report nobody changed, never overwrites an edited one nor recreates a deleted one.

## Versions

`major.minor.patch`, the artifact version equals `Spi.VERSION`.

| What changed | What to bump |
|---|---|
| a contract changed or was removed — old plugins break | major |
| something was added (a method with a default implementation, a type, a raw value) | minor |
| a fix that does not change contracts | patch |

The core loads a plugin if the major version matches and the plugin's minor is not newer than the core's
(`Spi.compatible`); patch does not matter. An incompatible plugin is not loaded, and the reason is shown in the admin
panel.

## Build

```
./mvnw -o install
```

After changes — `install` here, then build the visualms backend.

## Example

`examples/kpi` — a plugin on 1.2: WAULT by area and by income, the renewal rate of the last 12 months, the aging of
the debt. Build it after `install` here: `./mvnw -o -f examples/kpi/pom.xml package`; the jar goes into `plugins`.

## Changes

- **1.3.0** — ready reports: `ReportTemplate` (code, title, version, scope, widgets of catalog metrics) and
  `VisualmsPlugin.reportTemplates()`.
- **1.2.0** — data for portfolio KPIs:
  - `LeaseInfo` is one per lease and unit over all its approved versions and tells the lessee, the signing date, the
    planned end, how the lease ends (`EndReason`), how it is renewed (`RenewalKind`: amendment, auto-renewal, a new
    lease of the same lessee), the lease it follows, the monthly rent, industries and the tenant type; `yearsLeft`;
  - `UnitInfo.activeFrom / activeTo`, `existedOn`;
  - `ReportData.raw(name, filters, LocalDate at)` — a stock as of any day; `ReportData.deals()` — open deals
    (`DealInfo`);
  - `Filter.OVERDUE_BUCKET` (`bucket="8-14"`, `"32+"`, `"current"`, `DEFAULT_BUCKETS`) and `Filter.SECURITY_KIND`,
    raw values only (`Filter.rawOnly`).

  The records the core fills got their new components at the end and keep the older constructors.
- **1.1.0** — `Visualization.CUSTOM` (a custom widget: an ECharts template, a report script or a plugin widget type).
- **1.0.0** — first version: metrics, raw values, `ReportData`, widget types, `VisualmsPlugin`.

## License

[Apache License 2.0](LICENSE): plugins built on this SPI may be written and distributed under any terms, including
proprietary ones. The license covers only the SPI contracts, not the visualms core.
