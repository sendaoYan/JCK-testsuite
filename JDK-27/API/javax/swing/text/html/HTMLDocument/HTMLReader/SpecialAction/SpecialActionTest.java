/*
 * @test
 * @summary javax.swing.text.html.HTMLDocument.HTMLReader.SpecialAction conformance: white-box structure + black-box behavior.
 * @run main SpecialActionTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

public class SpecialActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLDocument$HTMLReader$SpecialAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.HTMLDocument$HTMLReader$TagAction"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        HTMLDocument doc = new HTMLDocument();
        HTMLDocument.HTMLReader reader = doc.new HTMLReader(0);
        HTMLDocument.HTMLReader.SpecialAction action = reader.new SpecialAction();
        check(action instanceof HTMLDocument.HTMLReader.TagAction, "extends TagAction");
        action.start(HTML.Tag.IMG, new SimpleAttributeSet());
        check(true, "SpecialAction start complete without error");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
