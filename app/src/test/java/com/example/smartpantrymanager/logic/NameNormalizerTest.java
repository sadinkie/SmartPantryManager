package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NameNormalizerTest {

    @Test
    public void tomatoVariants_allEqual() {
        String expected = NameNormalizer.normalize("tomato");
        assertEquals(expected, NameNormalizer.normalize("Tomatoes"));
        assertEquals(expected, NameNormalizer.normalize("  TOMATOES "));
        assertEquals(expected, NameNormalizer.normalize("fresh tomatoes"));
    }

    @Test
    public void commonPlurals() {
        assertEquals("egg", NameNormalizer.normalize("eggs"));
        assertEquals("potato", NameNormalizer.normalize("potatoes"));
        assertEquals("berry", NameNormalizer.normalize("berries"));
        assertEquals("spring onion", NameNormalizer.normalize("Spring Onions"));
    }

    @Test
    public void wordsThatShouldNotChange() {
        assertEquals("pasta", NameNormalizer.normalize("pasta"));
        assertEquals("rice", NameNormalizer.normalize("rice"));
        assertEquals("hummus", NameNormalizer.normalize("hummus"));
    }

    @Test
    public void nullAndBlank_returnEmpty() {
        assertEquals("", NameNormalizer.normalize(null));
        assertEquals("", NameNormalizer.normalize("   "));
    }
}