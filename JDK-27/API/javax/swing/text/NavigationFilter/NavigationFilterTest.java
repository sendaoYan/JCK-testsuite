/*
 * @test
 * @summary javax.swing.text.NavigationFilter conformance: white-box structure + black-box behavior.
 * @run main NavigationFilterTest
 */

import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;

public class NavigationFilterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.NavigationFilter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JTextArea area = new JTextArea();
        area.setText("hello world");

        // default pass-through filter
        NavigationFilter pass = new NavigationFilter();
        area.setNavigationFilter(pass);
        check(area.getNavigationFilter() == pass, "filter set");
        area.setCaretPosition(5);
        check(area.getCaretPosition() == 5, "default setDot passes through");
        area.moveCaretPosition(8);
        check(area.getCaretPosition() == 8 && area.getCaret().getMark() == 5, "default moveDot passes through");

        // clamping filter + FilterBypass
        final NavigationFilter.FilterBypass[] captured = new NavigationFilter.FilterBypass[1];
        area.setNavigationFilter(new NavigationFilter() {
            @Override
            public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
                captured[0] = fb;
                fb.setDot(Math.min(dot, 3), bias);
            }
            @Override
            public void moveDot(FilterBypass fb, int dot, Position.Bias bias) {
                captured[0] = fb;
                fb.moveDot(Math.min(dot, area.getDocument().getLength()), bias);
            }
        });
        area.setCaretPosition(10);
        check(area.getCaretPosition() == 3, "setDot clamped to 3 by filter");
        check(captured[0].getCaret() == area.getCaret(), "FilterBypass.getCaret returns the caret");

        area.setCaretPosition(0);
        area.moveCaretPosition(7);
        check(area.getCaretPosition() == 7 && area.getCaret().getMark() == 0, "moveDot clamped to document length");

        // FilterBypass direct calls bypass the filter
        NavigationFilter.FilterBypass fb = captured[0];
        fb.setDot(4, Position.Bias.Forward);
        check(area.getCaretPosition() == 4, "bypass setDot");
        fb.moveDot(9, Position.Bias.Forward);
        check(area.getCaretPosition() == 9 && area.getCaret().getMark() == 4, "bypass moveDot");

        // custom getNextVisualPositionFrom override
        area.setNavigationFilter(new NavigationFilter() {
            @Override
            public int getNextVisualPositionFrom(javax.swing.text.JTextComponent text, int pos,
                                                 Position.Bias bias, int direction, Position.Bias[] biasRet) {
                biasRet[0] = Position.Bias.Forward;
                return 42;
            }
        });
        Position.Bias[] biasRet = {Position.Bias.Backward};
        int v = area.getNavigationFilter().getNextVisualPositionFrom(
                area, 0, Position.Bias.Forward, SwingConstants.EAST, biasRet);
        check(v == 42, "getNextVisualPositionFrom override returns sentinel");
        check(biasRet[0] == Position.Bias.Forward, "biasRet populated by override");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
