/*
 * @test
 * @summary javax.swing.plaf.basic.BasicTextUI.BasicHighlighter conformance: white-box structure + black-box behavior.
 * @run main BasicHighlighterTest
 */
import java.lang.reflect.Modifier;

public class BasicHighlighterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasDeclaredMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicTextUI$BasicHighlighter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.DefaultHighlighter"), "superclass javax.swing.text.DefaultHighlighter");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");

    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicTextUI.BasicHighlighter h = new javax.swing.plaf.basic.BasicTextUI.BasicHighlighter();
        check(h instanceof javax.swing.plaf.UIResource, "UIResource marker");
        check(h instanceof javax.swing.text.Highlighter, "Highlighter");
        h.install(new javax.swing.JTextArea());
        javax.swing.text.Highlighter.HighlightPainter painter = new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(java.awt.Color.YELLOW);
        Object tag = h.addHighlight(0, 1, painter);
        check(h.getHighlights().length == 1, "one highlight added");
        h.removeHighlight(tag);
        check(h.getHighlights().length == 0, "highlight removed");
    }

}
