/*
 * @test
 * @summary javax.swing.text.Highlighter.HighlightPainter conformance: type shape via reflection (headless-safe).
 * @run main HighlightPainterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class HighlightPainterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Highlighter$HighlightPainter", false, HighlightPainterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("paint"), "method paint");

        // the default painter is a pure data holder usable headlessly
        javax.swing.text.DefaultHighlighter.DefaultHighlightPainter p1 =
                new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(java.awt.Color.YELLOW);
        check(p1.getColor().equals(java.awt.Color.YELLOW), "color retained");
        javax.swing.text.DefaultHighlighter.DefaultHighlightPainter p2 =
                new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(java.awt.Color.YELLOW);
        check(!p1.equals(p2), "painters use identity equality");
        check(p1.equals(p1), "identity equality with itself");
        javax.swing.text.DefaultHighlighter.DefaultHighlightPainter p3 =
                new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(java.awt.Color.RED);
        check(!p1.equals(p3) && !p2.equals(p3), "distinct instances never equal");
        check(p1 instanceof javax.swing.text.Highlighter.HighlightPainter, "is a HighlightPainter");
        check(p1 instanceof javax.swing.text.LayeredHighlighter.LayerPainter, "is a layered highlight painter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
