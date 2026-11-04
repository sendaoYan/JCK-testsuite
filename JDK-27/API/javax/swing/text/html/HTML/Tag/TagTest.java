/*
 * @test
 * @summary javax.swing.text.html.HTML.Tag conformance: white-box structure + black-box behavior.
 * @run main TagTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.html.HTML;

public class TagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTML$Tag");
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
        check(HTML.Tag.A.toString().equals("a"), "A toString");
        check(HTML.Tag.P.toString().equals("p"), "P toString");
        check(HTML.Tag.BODY.toString().equals("body"), "BODY toString");
        check(HTML.Tag.HTML.toString().equals("html"), "HTML toString");

        check(!HTML.Tag.A.isBlock(), "A not block");
        check(!HTML.Tag.A.breaksFlow(), "A not break");
        check(!HTML.Tag.A.isPreformatted(), "A not preformatted");

        check(HTML.Tag.P.isBlock(), "P block");
        check(HTML.Tag.P.breaksFlow(), "P breaks flow");
        check(!HTML.Tag.P.isPreformatted(), "P not preformatted");

        check(!HTML.Tag.BR.isBlock(), "BR not block");
        check(HTML.Tag.BR.breaksFlow(), "BR breaks flow");

        check(HTML.Tag.PRE.isPreformatted(), "PRE preformatted");
        check(HTML.Tag.TEXTAREA.isPreformatted(), "TEXTAREA preformatted");
        check(!HTML.Tag.DIV.isPreformatted(), "DIV not preformatted");

        HTML.Tag t = new HTML.Tag();
        check(!t.isBlock() && !t.breaksFlow() && !t.isPreformatted(), "default Tag flags");

        check(HTML.getTag("a") == HTML.Tag.A, "getTag identity");
        check(HTML.Tag.A != HTML.Tag.P, "distinct constants");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
