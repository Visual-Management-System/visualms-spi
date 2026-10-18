package ru.visualms.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpiTest {

    @Test
    void artifactVersionIsTheSpiVersion() {
        assertEquals(Spi.VERSION, System.getProperty("project.version").replace("-SNAPSHOT", ""));
    }

    @Test
    void sameMajorAndNotNewerMinorRuns() {
        assertTrue(Spi.compatible(Spi.MAJOR + "." + Spi.MINOR + ".0"));
        assertTrue(Spi.compatible(Spi.MAJOR + "." + Spi.MINOR + ".99"), "the patch does not matter");
        assertTrue(Spi.compatible(Spi.MAJOR + "." + Spi.MINOR), "the patch may be left out");
        assertNull(Spi.incompatibility(Spi.VERSION));
    }

    @Test
    void anotherMajorOrNewerMinorDoesNot() {
        assertFalse(Spi.compatible((Spi.MAJOR + 1) + ".0.0"));
        assertFalse(Spi.compatible((Spi.MAJOR - 1) + ".9.9"));
        assertFalse(Spi.compatible(Spi.MAJOR + "." + (Spi.MINOR + 1) + ".0"));
        assertNotNull(Spi.incompatibility(Spi.MAJOR + "." + (Spi.MINOR + 1) + ".0"));
    }

    @Test
    void malformedVersionIsAReasonNotACrash() {
        assertThrows(IllegalArgumentException.class, () -> Spi.compatible("1"));
        assertThrows(IllegalArgumentException.class, () -> Spi.compatible("1.x.0"));
        assertEquals("не указана версия SPI (Visualms-Spi-Version в манифесте)", Spi.incompatibility(null));
        assertNotNull(Spi.incompatibility("1.2.3.4"));
    }
}
