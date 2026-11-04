/*
 * @test
 * @summary java.awt.font.LineBreakMeasurer conformance: white-box structure plus black-box behavior.
 * @run main LineBreakMeasurerTest
 */

import java.awt.*;
import java.awt.font.*;
import java.awt.image.BufferedImage;
import java.text.*;
import java.lang.reflect.*;

public class LineBreakMeasurerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.LineBreakMeasurer");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(hasMethod(c, "nextOffset", float.class), "nextOffset(float)");
        check(hasMethod(c, "nextOffset", float.class, int.class, boolean.class), "nextOffset(float,int,boolean)");
        check(hasMethod(c, "nextLayout", float.class), "nextLayout(float)");
        check(hasMethod(c, "getPosition"), "getPosition()");
        check(hasMethod(c, "setPosition", int.class), "setPosition(int)");
        check(hasMethod(c, "insertChar", AttributedCharacterIterator.class, int.class), "insertChar");
        check(hasMethod(c, "deleteChar", AttributedCharacterIterator.class, int.class), "deleteChar");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Font f = new Font(Font.DIALOG, Font.PLAIN, 12);
        FontRenderContext frc = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().getFontRenderContext();
        AttributedString as = new AttributedString("one two three four");
        AttributedCharacterIterator iter = as.getIterator();

        LineBreakMeasurer lbm = new LineBreakMeasurer(iter, frc);
        check(lbm.getPosition() == 0, "initial position 0");

        int firstBreak = lbm.nextOffset(1000);
        check(firstBreak > 0 && firstBreak <= 18, "nextOffset within text");
        TextLayout line = lbm.nextLayout(1000);
        check(line != null && line.getAdvance() > 0, "nextLayout returns layout with advance");
        check(lbm.getPosition() == 18, "position advances to end of whole text");

        // setPosition reset
        lbm.setPosition(0);
        check(lbm.getPosition() == 0, "setPosition reset");

        // second constructor with explicit BreakIterator
        LineBreakMeasurer lbm2 = new LineBreakMeasurer(as.getIterator(), BreakIterator.getLineInstance(), frc);
        check(lbm2.getPosition() == 0, "second ctor initial position");

        // insertChar / deleteChar take the full new paragraph and reset position
        LineBreakMeasurer lbm3 = new LineBreakMeasurer(new AttributedString("abc def").getIterator(), frc);
        lbm3.insertChar(new AttributedString("aXbc def").getIterator(), 1);
        check(lbm3.getPosition() == 0, "insertChar resets position to begin");
        lbm3.deleteChar(new AttributedString("abc def").getIterator(), 1);
        check(lbm3.getPosition() == 0, "deleteChar resets position to begin");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
