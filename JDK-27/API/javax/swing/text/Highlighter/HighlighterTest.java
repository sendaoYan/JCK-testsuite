/*
 * @test
 * @summary javax.swing.text.Highlighter conformance: type shape via reflection (headless-safe).
 * @run main HighlighterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class HighlighterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Highlighter", false, HighlighterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("install"), "method install");
        check(names.contains("deinstall"), "method deinstall");
        check(names.contains("paint"), "method paint");
        check(names.contains("addHighlight"), "method addHighlight");
        check(names.contains("removeHighlight"), "method removeHighlight");
        check(names.contains("removeAllHighlights"), "method removeAllHighlights");
        check(names.contains("changeHighlight"), "method changeHighlight");
        check(names.contains("getHighlights"), "method getHighlights");

        // nested types: Highlight (a rendered range + painter) and HighlightPainter
        Class<?> hl = Class.forName("javax.swing.text.Highlighter$Highlight", false, HighlighterTest.class.getClassLoader());
        check(hl.isInterface(), "Highlight is an interface");
        Set<String> hn = new HashSet<String>();
        for (Method m : hl.getMethods()) hn.add(m.getName());
        check(hn.contains("getStartOffset"), "Highlight getStartOffset");
        check(hn.contains("getEndOffset"), "Highlight getEndOffset");
        check(hn.contains("getPainter"), "Highlight getPainter");
        Class<?> hp = Class.forName("javax.swing.text.Highlighter$HighlightPainter", false, HighlighterTest.class.getClassLoader());
        check(hp.isInterface(), "HighlightPainter is an interface");
        check(hp.getInterfaces().length == 0, "HighlightPainter extends nothing (serializable only via implementations)");
        Set<String> pn = new HashSet<String>();
        for (Method m : hp.getMethods()) pn.add(m.getName());
        check(pn.contains("paint"), "HighlightPainter paint");

        // the default implementation covers the same surface
        javax.swing.text.Highlighter def = new javax.swing.text.DefaultHighlighter();
        check(def != null, "DefaultHighlighter instantiable headlessly");
        check(def.getHighlights().length == 0, "fresh highlighter has no highlights");
        // DefaultHighlighter.DefaultHighlightPainter is the shared painter
        javax.swing.text.DefaultHighlighter.DefaultHighlightPainter painter =
                new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(java.awt.Color.YELLOW);
        check(painter.getColor().equals(java.awt.Color.YELLOW), "painter color retained");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
