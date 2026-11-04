/*
 * @test
 * @summary javax.swing.text.html.StyleSheet.ListPainter conformance: white-box structure + black-box behavior.
 * @run main ListPainterTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.StyleSheet;

public class ListPainterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.StyleSheet$ListPainter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        StyleSheet ss = new StyleSheet();
        AttributeSet plain = new SimpleAttributeSet();
        StyleSheet.ListPainter lp = ss.getListPainter(plain);
        check(lp != null, "getListPainter non-null");
        check(lp instanceof StyleSheet.ListPainter, "instanceof ListPainter");

        AttributeSet cssAttrs = ss.getDeclaration("list-style-type: square");
        StyleSheet.ListPainter lp2 = ss.getListPainter(cssAttrs);
        check(lp2 != null, "getListPainter with css attrs non-null");
        check(lp2 != lp, "distinct ListPainter per call");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
