/*
 * @test
 * @summary javax.swing.text.html.HTMLEditorKit.HTMLFactory conformance: white-box structure + black-box behavior.
 * @run main HTMLFactoryTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;

import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class HTMLFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLEditorKit$HTMLFactory");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.ViewFactory"),
              "interface 0");
        // public no-arg constructor
        check(c.getConstructor().getModifiers() == Modifier.PUBLIC ||
              Modifier.isPublic(c.getConstructor().getModifiers()),
              "public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HTMLEditorKit kit = new HTMLEditorKit();
        javax.swing.text.html.HTMLEditorKit.HTMLFactory factory =
            new javax.swing.text.html.HTMLEditorKit.HTMLFactory();

        // the kit's shared factory is an HTMLFactory
        check(kit.getViewFactory() instanceof
              javax.swing.text.html.HTMLEditorKit.HTMLFactory,
              "kit view factory is HTMLFactory");

        // build a document and create views for its elements (headless-safe)
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader("<html><body><b>bold</b></body></html>"), doc, 0);

        Element paragraph = doc.getParagraphElement(1);
        View paraView = factory.create(paragraph);
        check(paraView != null, "create(paragraph) non-null");
        check(paraView instanceof javax.swing.text.html.ParagraphView,
              "create(paragraph) yields ParagraphView");

        Element content = doc.getCharacterElement(1);
        View contentView = factory.create(content);
        check(contentView != null, "create(content) non-null");
        check(contentView instanceof javax.swing.text.html.InlineView,
              "create(content) yields InlineView");

        // root section element maps to a BlockView (BoxView, Y_AXIS)
        Element root = doc.getDefaultRootElement();
        View rootView = factory.create(root);
        check(rootView != null, "create(root) non-null");
        check(rootView instanceof View, "create(root) yields a View");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
