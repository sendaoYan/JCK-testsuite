/*
 * @test
 * @summary javax.swing.text.NavigationFilter.FilterBypass conformance: white-box structure + black-box behavior.
 * @run main FilterBypassTest
 */

import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;

public class FilterBypassTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.NavigationFilter$FilterBypass");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JTextArea area = new JTextArea("hello world");
        final NavigationFilter.FilterBypass[] captured = new NavigationFilter.FilterBypass[1];
        area.setNavigationFilter(new NavigationFilter() {
            @Override
            public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
                captured[0] = fb;
                fb.setDot(dot, bias);
            }
        });

        area.setCaretPosition(6);
        NavigationFilter.FilterBypass bypass = captured[0];
        check(bypass != null, "bypass captured from filter");
        check(bypass.getCaret() == area.getCaret(), "getCaret returns the changing caret");

        bypass.setDot(2, Position.Bias.Forward);
        check(area.getCaretPosition() == 2 && area.getCaret().getMark() == 2, "bypass setDot");

        bypass.moveDot(5, Position.Bias.Forward);
        check(area.getCaretPosition() == 5 && area.getCaret().getMark() == 2, "bypass moveDot");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
