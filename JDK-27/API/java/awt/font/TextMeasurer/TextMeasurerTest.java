/*
 * @test
 * @summary java.awt.font.TextMeasurer conformance: white-box structure plus black-box behavior.
 * @run main TextMeasurerTest
 */

import java.awt.*;
import java.awt.font.*;
import java.awt.image.BufferedImage;
import java.text.*;
import java.lang.reflect.*;

public class TextMeasurerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.TextMeasurer");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getLineBreakIndex", int.class, float.class), "getLineBreakIndex(int,float)");
        check(hasMethod(c, "getAdvanceBetween", int.class, int.class), "getAdvanceBetween(int,int)");
        check(hasMethod(c, "getLayout", int.class, int.class), "getLayout(int,int)");
        check(hasMethod(c, "insertChar", AttributedCharacterIterator.class, int.class), "insertChar");
        check(hasMethod(c, "deleteChar", AttributedCharacterIterator.class, int.class), "deleteChar");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Font f = new Font(Font.DIALOG, Font.PLAIN, 12);
        FontRenderContext frc = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().getFontRenderContext();
        AttributedString as = new AttributedString("one two three four");
        TextMeasurer tm = new TextMeasurer(as.getIterator(), frc);

        int breakIdx = tm.getLineBreakIndex(0, 1000);
        check(breakIdx > 0 && breakIdx <= 18, "getLineBreakIndex within text");
        float adv = tm.getAdvanceBetween(0, 3);
        check(adv > 0, "getAdvanceBetween positive");
        TextLayout layout = tm.getLayout(0, 3);
        check(layout.getCharacterCount() == 3, "getLayout(0,3) char count 3");

        // insertChar / deleteChar take the full new paragraph
        TextMeasurer tm2 = new TextMeasurer(new AttributedString("abc def").getIterator(), frc);
        tm2.insertChar(new AttributedString("aXbc def").getIterator(), 1);
        check(tm2.getLineBreakIndex(0, 1000) >= 0, "insertChar keeps measurer usable");
        tm2.deleteChar(new AttributedString("abc def").getIterator(), 1);
        check(tm2.getAdvanceBetween(0, 3) > 0, "deleteChar keeps measurer usable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
