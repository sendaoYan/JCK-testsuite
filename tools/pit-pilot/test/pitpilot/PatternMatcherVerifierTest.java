package pitpilot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PatternMatcherVerifierTest {

    @Test
    public void matchesAllTrueWhenAllMatch() {
        assertTrue(PatternMatcherVerifier.matchesAll("\\d+", "0", "123", "9999"));
    }

    @Test
    public void matchesAllFalseWhenAnyMismatch() {
        assertFalse(PatternMatcherVerifier.matchesAll("\\d+", "0", "12x", "9999"));
        assertFalse(PatternMatcherVerifier.matchesAll("\\d+", "abc"));
    }

    @Test
    public void countMatchesCountsOverlappingFreeOccurrences() {
        assertEquals(0, PatternMatcherVerifier.countMatches("z", "abc"));
        assertEquals(3, PatternMatcherVerifier.countMatches("a", "banana"));
        assertEquals(1, PatternMatcherVerifier.countMatches("^\\d+$", "123"));
    }

    @Test
    public void matchesAnyTrueWithNullsAndMatches() {
        assertTrue(PatternMatcherVerifier.matchesAny("\\d+", null, "x", "42"));
    }

    @Test
    public void matchesAnyFalseWhenNoneMatch() {
        assertFalse(PatternMatcherVerifier.matchesAny("\\d+", "x", "y", null, ""));
    }
}
