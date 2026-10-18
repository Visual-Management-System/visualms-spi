package ru.visualms.spi;

import java.util.List;
import java.util.Set;

/**
 * A metric of the catalog: what it means and how it can be shown. Reports are built from metrics — no SQL or scripts
 * are stored; the numbers are computed on the server with the viewer's rights.
 *
 * @param code     stable id, e.g. {@code occupancy.area_pct}; stored in widgets
 * @param group    for the catalog, e.g. «Заполняемость»
 * @param scopes   where the metric makes sense: one building and / or the portfolio
 * @param finance  the part of the finances a viewer needs to see it; null = no money in it
 * @param defaults visualization offered first
 * @param period   the metric covers a number of months (series, sums over a period): widgets set {@code months}
 * @param defaultMonths the period offered first
 * @param filters  what the viewer may narrow the metric by on the dashboard (none — the whole scope)
 * @param range    how the viewer may choose its time on the dashboard besides the widget's months
 * @param options  further choices of the viewer: a daily step, the measure (count / area / percent)
 */
public record MetricDescriptor(String code, String title, String description, String group, Set<Scope> scopes,
                               Shape shape, Unit unit, Finance finance, Visualization defaults, boolean period,
                               Integer defaultMonths, Set<Filter> filters, Range range,
                               Set<Option> options) {

    /** What a dashboard is about. */
    public enum Scope {
        /** One building, chosen when the dashboard is opened */
        BUILDING("Здание"),
        /** Every building the viewer sees (their working set) */
        PORTFOLIO("Портфель");

        private final String title;

        Scope(String title) {
            this.title = title;
        }

        public String title() {
            return title;
        }
    }

    /** What the metric returns. */
    public enum Shape {
        /** One number (KPI), optionally with a comparison */
        VALUE(List.of(Visualization.KPI)),
        /** Values by month */
        SERIES(List.of(Visualization.LINE, Visualization.COLUMN, Visualization.AREA, Visualization.STACKED_AREA,
                Visualization.STACKED_COLUMN, Visualization.TABLE)),
        /** Values by category (floor, kind of payment, bucket…) */
        BREAKDOWN(List.of(Visualization.PIE, Visualization.BAR, Visualization.COLUMN, Visualization.TABLE)),
        /** Rows with columns */
        TABLE(List.of(Visualization.TABLE));

        private final List<Visualization> visualizations;

        Shape(List<Visualization> visualizations) {
            this.visualizations = visualizations;
        }

        public List<Visualization> visualizations() {
            return visualizations;
        }
    }

    public enum Visualization {
        KPI("Показатель"), LINE("Линия"), COLUMN("Столбцы"), AREA("Область"), STACKED_AREA("Области с накоплением"),
        STACKED_COLUMN("Столбцы с накоплением"), PIE("Круговая"), BAR("Горизонтальные полосы"), TABLE("Таблица"),
        /** Drawn by code: an ECharts template or a script of the report (ADMIN), or a plugin's widget type; since 1.1 */
        CUSTOM("Свой виджет");

        private final String title;

        Visualization(String title) {
            this.title = title;
        }

        public String title() {
            return title;
        }
    }

    /**
     * What a metric can be narrowed by. {@link #OVERDUE_BUCKET} and {@link #SECURITY_KIND} narrow raw values only and
     * are never offered to the viewer of a dashboard ({@link #rawOnly()}).
     */
    public enum Filter {
        FLOOR("floor", "Этаж"), PAYMENT_KIND("kind", "Вид платежа"), UNIT_PURPOSE("purpose", "Тип объекта аренды"),
        INDUSTRY("industry", "Отраслевое направление"), LEASE("lease", "Договор"),
        /**
         * Days past due: {@code current} — not due yet, {@code 8-14} — from 8 to 14 days, {@code 32+} — 32 days and
         * more; any bounds may be given. The buckets of the core by default: {@link #DEFAULT_BUCKETS}. Since 1.2
         */
        OVERDUE_BUCKET("bucket", "Срок просрочки"),
        /** The kind of a security deposit line, by its name («Депозит», «Банковская гарантия»…). Since 1.2 */
        SECURITY_KIND("security", "Вид обеспечения");

        /** The overdue buckets of the core by default — the same as its own aging. Since 1.2 */
        public static final List<String> DEFAULT_BUCKETS = List.of("current", "1-7", "8-14", "15-31", "32+");

        private final String key;
        private final String title;

        Filter(String key, String title) {
            this.key = key;
            this.title = title;
        }

        /** Narrows raw values only, never offered on a dashboard. Since 1.2 */
        public boolean rawOnly() {
            return this == OVERDUE_BUCKET || this == SECURITY_KIND;
        }

        /** The name of the filter in a formula: {@code accrued(kind="Базовая арендная плата")} */
        public String key() {
            return key;
        }

        public String title() {
            return title;
        }
    }

    /**
     * The time of a metric the viewer may choose: {@code DATE} — a day (as of), {@code MONTH} — one month,
     * {@code SPAN} — months from / to (the future too, where there is a plan); {@code NONE} — only the widget's months.
     */
    public enum Range { NONE, DATE, MONTH, SPAN }

    /** {@code DAY_STEP} — a series by days as well as by months; {@code MEASURE} — count, area or percent. */
    public enum Option { DAY_STEP, MEASURE }

    /**
     * The unit of a metric. Money is neutral — {@link #MONEY} and {@link #MONEY_PER_M2} are in the base currency of
     * the instance (rubles in Russia, euros in the EU), its sign is given by the host ({@link ReportData#moneySign()}).
     */
    public enum Unit {
        M2("м²"), PERCENT("%"), COUNT("шт."), MONEY("%s"), MONEY_PER_M2("%s/м²/мес"), YEARS("лет"), DAYS("дн."),
        MONTHS("мес."), MIXED("");

        private final String title;

        Unit(String title) {
            this.title = title;
        }

        /** The title with the sign of the base currency put in for money. */
        public String title(String moneySign) {
            return title.replace("%s", moneySign);
        }
    }

    public boolean allows(Visualization v) {
        return shape.visualizations().contains(v);
    }

    /** The same metric with what the viewer may choose on the dashboard. */
    public MetricDescriptor with(Set<Filter> filters, Range range, Option... options) {
        return new MetricDescriptor(code, title, description, group, scopes, shape, unit, finance, defaults, period,
                defaultMonths, Set.copyOf(filters), range, Set.of(options));
    }
}
