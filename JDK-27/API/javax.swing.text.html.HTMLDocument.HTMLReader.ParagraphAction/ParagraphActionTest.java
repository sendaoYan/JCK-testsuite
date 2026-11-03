/*
 * @test
 * @summary javax.swing.text.html.HTMLDocument.HTMLReader.ParagraphAction conformance: white-box structure + black-box behavior.
 * @run main ParagraphActionTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

public class ParagraphActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLDocument$HTMLReader$ParagraphAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.HTMLDocument$HTMLReader$BlockAction"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        HTMLDocument doc = new HTMLDocument();
        HTMLDocument.HTMLReader reader = doc.new HTMLReader(0);
        HTMLDocument.HTMLReader.ParagraphAction action = reader.new ParagraphAction();
        check(action instanceof HTMLDocument.HTMLReader.BlockAction, "extends BlockAction");
        action.start(HTML.Tag.P, new SimpleAttributeSet());
        action.end(HTML.Tag.P);
        check(true, "ParagraphAction start/end complete without error");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
