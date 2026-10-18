package ru.visualms.spi;

/**
 * The parts of the finances of a lease that a user may see separately. A metric or a raw value with money declares
 * the part it needs; without it the viewer gets the metric hidden.
 */
public enum Finance {
    /** Lease terms: rate, fixed payment, indexation */
    RENT_TERMS("условия договоров"),
    /** Accrued */
    ACCRUED("начисления"),
    /** Paid */
    PAID("оплаты"),
    /** Debt / balance */
    BALANCE("задолженность"),
    /** Tenant turnover and the turnover rent percentage */
    TURNOVER("товарооборот");

    private final String title;

    Finance(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
