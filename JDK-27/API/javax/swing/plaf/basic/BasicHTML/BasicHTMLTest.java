/*
 * @test
 * @summary javax.swing.plaf.basic.BasicHTML conformance: white-box structure + black-box behavior.
 * @run main BasicHTMLTest
 */

import java.lang.reflect.Modifier;

public class BasicHTMLTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicHTML");
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

    static void testBlackBox() throws Exception {
        check(javax.swing.plaf.basic.BasicHTML.isHTMLString("<html>body</html>"), "html true");
        check(javax.swing.plaf.basic.BasicHTML.isHTMLString("<HTML>"), "case-insensitive html");
        check(!javax.swing.plaf.basic.BasicHTML.isHTMLString("<body>"), "non-html false");
        check(!javax.swing.plaf.basic.BasicHTML.isHTMLString(null), "null false");
        check(!javax.swing.plaf.basic.BasicHTML.isHTMLString("<htm"), "too short false");
        javax.swing.JLabel lab = new javax.swing.JLabel();
        javax.swing.text.View v = javax.swing.plaf.basic.BasicHTML.createHTMLView(lab, "<html><b>hi</b></html>");
        check(v != null, "createHTMLView non-null");
        javax.swing.plaf.basic.BasicHTML.updateRenderer(lab, "<html><i>hi</i></html>");
        check(lab.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null, "updateRenderer sets property");
        int baseline = javax.swing.plaf.basic.BasicHTML.getHTMLBaseline(v, 100, 30);
        check(baseline >= -1, "html baseline sane");
    }
}
