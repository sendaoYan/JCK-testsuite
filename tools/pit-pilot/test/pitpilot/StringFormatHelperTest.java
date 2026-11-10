package pitpilot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class StringFormatHelperTest {

    @Test
    public void zeroPadFixedWidth() {
        assertEquals("00042", StringFormatHelper.zeroPadFormat(42, 5));
        assertEquals("7", StringFormatHelper.zeroPadFormat(7, 1));
        assertEquals("-0007", StringFormatHelper.zeroPadFormat(-7, 5));
        assertEquals("0000", StringFormatHelper.zeroPadFormat(0, 4));
    }

    @Test
    public void zeroPadRejectsNonPositiveWidth() {
        // 强断言：宽度 0 必须抛出「width must be positive」这个具体异常消息，
        // 而非下游 String.format 抛出的 DuplicateFormatFlagsException（同属 IllegalArgumentException）。
        try {
            StringFormatHelper.zeroPadFormat(1, 0);
            fail("expected IllegalArgumentException for width == 0");
        } catch (IllegalArgumentException e) {
            assertEquals("width must be positive", e.getMessage());
        }
    }

    @Test
    public void joinInvariant() {
        assertTrue(StringFormatHelper.isJoined("a,b,c", ",", Arrays.asList("a", "b", "c")));
        assertTrue(StringFormatHelper.isJoined("", ",", Collections.<String>emptyList()));
        assertFalse(StringFormatHelper.isJoined("a,b", ",", Arrays.asList("a", "b", "c")));
        assertFalse(StringFormatHelper.isJoined("a,b,c", ";", Arrays.asList("a", "b", "c")));
    }

    @Test
    public void repeatInvariant() {
        assertTrue(StringFormatHelper.isRepeated("ababab", "ab", 3));
        assertTrue(StringFormatHelper.isRepeated("", "ab", 0));
        assertFalse(StringFormatHelper.isRepeated("abab", "ab", 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void repeatRejectsNegativeCount() {
        StringFormatHelper.isRepeated("", "ab", -1);
    }

    @Test
    public void titleCaseNormalizes() {
        assertEquals("Hello", StringFormatHelper.titleCase("hELLO"));
        assertEquals("Foo", StringFormatHelper.titleCase("foo"));
        assertEquals("A", StringFormatHelper.titleCase("a"));
        assertEquals("", StringFormatHelper.titleCase(""));
        assertNull(StringFormatHelper.titleCase(null));
    }
}
