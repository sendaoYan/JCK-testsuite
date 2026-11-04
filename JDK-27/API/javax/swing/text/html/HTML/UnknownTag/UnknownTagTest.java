/*
 * @test
 * @summary javax.swing.text.html.HTML.UnknownTag conformance: white-box structure + black-box behavior.
 * @run main UnknownTagTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.html.HTML;

public class UnknownTagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTML$UnknownTag");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.HTML$Tag"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        HTML.UnknownTag t = new HTML.UnknownTag("mycustom");
        check(t != null, "construct");
        check(t.toString().equals("mycustom"), "toString");
        check(t instanceof HTML.Tag, "instanceof Tag");
        check(!t.isBlock(), "not block");
        check(!t.breaksFlow(), "not break");
        check(!t.isPreformatted(), "not preformatted");

        HTML.UnknownTag same = new HTML.UnknownTag("mycustom");
        HTML.UnknownTag other = new HTML.UnknownTag("other");
        check(t.equals(same), "equals same id");
        check(t.hashCode() == same.hashCode(), "hashCode same id");
        check(!t.equals(other), "not equals different id");
        check(!t.equals("mycustom"), "not equals String");
        check(!t.equals(null), "not equals null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
