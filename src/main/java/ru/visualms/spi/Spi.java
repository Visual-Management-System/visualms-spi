package ru.visualms.spi;

/**
 * The version of the SPI, {@code major.minor.patch}:
 * <ul>
 *   <li>major — a contract changed or was removed: plugins of another major break;</li>
 *   <li>minor — something was added: a plugin of a newer minor may call what an older core lacks. Records the core
 *       fills ({@link LeaseInfo}, {@link UnitInfo}) get new components at the end and keep their older constructor:
 *       plugins only read them;</li>
 *   <li>patch — a fix without any change of the contracts.</li>
 * </ul>
 * A plugin names the SPI it was built against in its jar's manifest, {@code Visualms-Spi-Version: 1.0.0}; the core
 * reads it before loading any class of the plugin and loads it only when {@link #compatible(String)}.
 */
public final class Spi {

    public static final int MAJOR = 1;
    public static final int MINOR = 3;
    public static final int PATCH = 0;
    public static final String VERSION = MAJOR + "." + MINOR + "." + PATCH;

    /** The manifest attribute of a plugin's jar with the SPI version it was built against. */
    public static final String MANIFEST_ATTRIBUTE = "Visualms-Spi-Version";

    private Spi() {
    }

    /**
     * A plugin built against {@code version} runs on this SPI: the same major and a minor not newer; the patch does
     * not matter.
     *
     * @throws IllegalArgumentException not {@code major.minor[.patch]}
     */
    public static boolean compatible(String version) {
        int[] v = parse(version);
        return v[0] == MAJOR && v[1] <= MINOR;
    }

    /** Why a plugin built against {@code version} does not run here; null — it does. */
    public static String incompatibility(String version) {
        int[] v;
        try {
            v = parse(version);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        if (v[0] != MAJOR) {
            return "собран под SPI " + version + ", ядро поддерживает " + MAJOR + ".x";
        }
        if (v[1] > MINOR) {
            return "собран под SPI " + version + ", нужна версия ядра с SPI не ниже " + v[0] + "." + v[1]
                    + " (сейчас " + VERSION + ")";
        }
        return null;
    }

    private static int[] parse(String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("не указана версия SPI (" + MANIFEST_ATTRIBUTE + " в манифесте)");
        }
        String[] parts = version.trim().split("\\.");
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("версия SPI «" + version + "» не вида 1.0.0");
        }
        int[] v = new int[3];
        try {
            for (int i = 0; i < parts.length; i++) {
                v[i] = Integer.parseInt(parts[i]);
                if (v[i] < 0) {
                    throw new NumberFormatException();
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("версия SPI «" + version + "» не вида 1.0.0");
        }
        return v;
    }
}
