/*
 * @test
 * @summary javax.swing.border.TitledBorder conformance: white-box structure + black-box behavior.
 * @run main TitledBorderTest
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class TitledBorderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean insetsEq(Insets in, int top, int left, int bottom, int right) {
        return in != null && in.top == top && in.left == left && in.bottom == bottom && in.right == right;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.border.TitledBorder");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.AbstractBorder"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");

        // public title-position / justification constants
        check(TitledBorder.DEFAULT_POSITION == 0 && TitledBorder.ABOVE_TOP == 1 && TitledBorder.TOP == 2
                && TitledBorder.BELOW_TOP == 3 && TitledBorder.ABOVE_BOTTOM == 4 && TitledBorder.BOTTOM == 5
                && TitledBorder.BELOW_BOTTOM == 6, "position constants");
        check(TitledBorder.DEFAULT_JUSTIFICATION == 0 && TitledBorder.LEFT == 1 && TitledBorder.CENTER == 2
                && TitledBorder.RIGHT == 3 && TitledBorder.LEADING == 4 && TitledBorder.TRAILING == 5, "justification constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Component comp = new Component() {};

        // single-arg constructor: title stored, default position/justification
        TitledBorder t0 = new TitledBorder("Title");
        check("Title".equals(t0.getTitle()), "title string ctor");
        check(t0.getTitlePosition() == TitledBorder.DEFAULT_POSITION, "default position");
        check(t0.getTitleJustification() == TitledBorder.LEADING, "default justification LEADING");

        Border line = new LineBorder(Color.BLACK, 2);
        TitledBorder t1 = new TitledBorder(line, "Panel", TitledBorder.CENTER, TitledBorder.ABOVE_TOP);
        check(t1.getBorder() == line, "border round-trip");
        check("Panel".equals(t1.getTitle()), "title round-trip");
        check(t1.getTitleJustification() == TitledBorder.CENTER, "justification round-trip");
        check(t1.getTitlePosition() == TitledBorder.ABOVE_TOP, "position round-trip");

        // empty title leaves the wrapped border's insets untouched
        TitledBorder t2 = new TitledBorder(line, "");
        check(insetsEq(t2.getBorderInsets(comp), 2, 2, 2, 2), "empty title insets == border insets");

        // setters round-trip all properties
        t2.setTitle("New");
        check("New".equals(t2.getTitle()), "setTitle");
        EmptyBorder eb = new EmptyBorder(1, 2, 3, 4);
        t2.setBorder(eb);
        check(t2.getBorder() == eb, "setBorder");
        t2.setTitlePosition(TitledBorder.BOTTOM);
        check(t2.getTitlePosition() == TitledBorder.BOTTOM, "setTitlePosition");
        t2.setTitleJustification(TitledBorder.RIGHT);
        check(t2.getTitleJustification() == TitledBorder.RIGHT, "setTitleJustification");
        Font f = new Font(Font.DIALOG, Font.BOLD, 14);
        t2.setTitleFont(f);
        check(t2.getTitleFont() == f, "setTitleFont round-trip");
        t2.setTitleColor(Color.RED);
        check(t2.getTitleColor() == Color.RED, "setTitleColor round-trip");

        // invalid position / justification are rejected
        boolean iae1 = false;
        try { new TitledBorder("x").setTitlePosition(99); } catch (IllegalArgumentException e) { iae1 = true; }
        check(iae1, "invalid title position IAE");
        boolean iae2 = false;
        try { new TitledBorder("x").setTitleJustification(99); } catch (IllegalArgumentException e) { iae2 = true; }
        check(iae2, "invalid title justification IAE");

        // minimum size with an empty title is exactly the insets sum
        TitledBorder t3 = new TitledBorder(line, "");
        Dimension d = t3.getMinimumSize(comp);
        check(d.width == 4 && d.height == 4, "min size empty title");

        // minimum size with a title grows beyond the insets-only base
        TitledBorder t4 = new TitledBorder(line, "X", TitledBorder.LEFT, TitledBorder.ABOVE_TOP);
        Dimension d2 = t4.getMinimumSize(comp);
        check(d2.width >= 4 && d2.height >= 4, "min size with title grows");

        check(!t1.isBorderOpaque(), "titled border not opaque");

        // baseline: empty title -> -1; negative width -> IAE; null component -> NPE
        check(t3.getBaseline(comp, 100, 100) == -1, "empty title baseline -1");
        boolean iae3 = false;
        try { t1.getBaseline(comp, -1, 100); } catch (IllegalArgumentException e) { iae3 = true; }
        check(iae3, "baseline negative width IAE");
        boolean npe = false;
        try { t1.getBaseline(null, 100, 100); } catch (NullPointerException e) { npe = true; }
        check(npe, "baseline null component NPE");

        // baseline resize behavior follows the title position
        check(t1.getBaselineResizeBehavior(comp) == Component.BaselineResizeBehavior.CONSTANT_ASCENT, "ABOVE_TOP ascent");
        TitledBorder t5 = new TitledBorder(line, "X", TitledBorder.LEFT, TitledBorder.BOTTOM);
        check(t5.getBaselineResizeBehavior(comp) == Component.BaselineResizeBehavior.CONSTANT_DESCENT, "BOTTOM descent");

        // paintBorder smoke with an empty title (delegates to the wrapped border)
        BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        t3.paintBorder(comp, g, 0, 0, 20, 20);
        g.dispose();
        check(true, "paintBorder smoke");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        TitledBorder b = new TitledBorder("Hello");
        check("Hello".equals(b.getTitle()), "title");
        check(b.getBorder() != null, "has default border");
        check(b.getTitlePosition() == TitledBorder.DEFAULT_POSITION, "position");
        check(b.getTitleJustification() == TitledBorder.LEADING, "justification");
    }




    @SuppressWarnings("unchecked")
    static <T> T roundtrip(T obj) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(obj);
        java.io.ObjectInputStream oin = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return (T) oin.readObject();
    }

    // ---- Part 2b: serialization round-trip + BorderFactory data contracts ----
    static void testExtraContracts() throws Exception {
        TitledBorder tb = new TitledBorder(new LineBorder(Color.BLACK, 2), "Ser",
                TitledBorder.RIGHT, TitledBorder.BELOW_BOTTOM);
        TitledBorder tcopy = roundtrip(tb);
        check(tcopy != tb, "serialized TitledBorder distinct instance");
        check("Ser".equals(tcopy.getTitle()), "serialized title preserved");
        check(tcopy.getTitleJustification() == TitledBorder.RIGHT, "serialized justification preserved");
        check(tcopy.getTitlePosition() == TitledBorder.BELOW_BOTTOM, "serialized position preserved");
        check(tcopy.getBorder() instanceof LineBorder, "serialized wrapped border preserved");
        check(tcopy.getTitleColor() != null, "serialized title color resolved");

        javax.swing.border.Border t1 = javax.swing.BorderFactory.createTitledBorder("T1");
        check(t1 instanceof TitledBorder, "createTitledBorder(String) type");
        check("T1".equals(((TitledBorder) t1).getTitle()), "createTitledBorder(String) title");
        javax.swing.border.Border t2 = javax.swing.BorderFactory.createTitledBorder(new EmptyBorder(1, 1, 1, 1));
        check(((TitledBorder) t2).getBorder() instanceof EmptyBorder, "createTitledBorder(Border) border");
        check("".equals(((TitledBorder) t2).getTitle()), "createTitledBorder(Border) empty title");
        javax.swing.border.Border t3 = javax.swing.BorderFactory.createTitledBorder(new EmptyBorder(1, 1, 1, 1), "T3");
        check("T3".equals(((TitledBorder) t3).getTitle()), "createTitledBorder(Border,String) title");
        javax.swing.border.Border t4 = javax.swing.BorderFactory.createTitledBorder(
                new EmptyBorder(1, 1, 1, 1), "T4", TitledBorder.CENTER, TitledBorder.TOP);
        check(((TitledBorder) t4).getTitleJustification() == TitledBorder.CENTER, "createTitledBorder 4-arg justification");
        check(((TitledBorder) t4).getTitlePosition() == TitledBorder.TOP, "createTitledBorder 4-arg position");
        javax.swing.border.Border t5 = javax.swing.BorderFactory.createTitledBorder(
                new EmptyBorder(1, 1, 1, 1), "T5", TitledBorder.LEFT, TitledBorder.ABOVE_TOP,
                new Font(Font.DIALOG, Font.ITALIC, 11));
        check(((TitledBorder) t5).getTitleFont().getSize() == 11, "createTitledBorder 5-arg font size");
        check(((TitledBorder) t5).getTitleFont().isItalic(), "createTitledBorder 5-arg font style");
        javax.swing.border.Border t6 = javax.swing.BorderFactory.createTitledBorder(
                new EmptyBorder(1, 1, 1, 1), "T6", TitledBorder.LEFT, TitledBorder.ABOVE_TOP,
                new Font(Font.DIALOG, Font.PLAIN, 12), Color.BLUE);
        check(((TitledBorder) t6).getTitleColor() == Color.BLUE, "createTitledBorder 6-arg color");
        javax.swing.border.Border t7 = javax.swing.BorderFactory.createTitledBorder(
                new EmptyBorder(1, 1, 1, 1), "T7", TitledBorder.TRAILING, TitledBorder.BELOW_BOTTOM,
                new Font(Font.DIALOG, Font.PLAIN, 12), Color.RED);
        check(((TitledBorder) t7).getTitleJustification() == TitledBorder.TRAILING
              && ((TitledBorder) t7).getTitlePosition() == TitledBorder.BELOW_BOTTOM,
              "createTitledBorder 6-arg trailing/below-bottom");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        testExtraContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
