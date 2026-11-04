/*
 * @test
 * @summary javax.swing.text.html.StyleSheet.BoxPainter conformance: white-box structure + black-box behavior.
 * @run main BoxPainterTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.StyleSheet;

public class BoxPainterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.StyleSheet$BoxPainter");
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
        StyleSheet.BoxPainter bp = ss.getBoxPainter(plain);
        check(bp != null, "getBoxPainter non-null");
        check(bp instanceof StyleSheet.BoxPainter, "instanceof BoxPainter");

        AttributeSet cssAttrs = ss.getDeclaration("margin-top: 10px; padding: 2px");
        StyleSheet.BoxPainter bp2 = ss.getBoxPainter(cssAttrs);
        check(bp2 != null, "getBoxPainter with css attrs non-null");
        check(bp2 != bp, "distinct BoxPainter per call");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
