/*
 * @test
 * @summary javax.swing.JEditorPane conformance: white-box structure + black-box behavior.
 * @run main JEditorPaneTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkListener;

public class JEditorPaneTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JEditorPane");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.JTextComponent"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JEditorPane pane = new JEditorPane();
        check("text/plain".equals(pane.getContentType()), "default content type");
        check(pane.getEditorKit() != null, "getEditorKit");
        check(pane.getPage() == null, "page initially null");

        pane.setText("hello");
        check("hello".equals(pane.getText()), "setText/getText");
        pane.setContentType("text/html");
        check("text/html".equals(pane.getContentType()), "setContentType");

        HyperlinkListener l = e -> { };
        pane.addHyperlinkListener(l);
        pane.removeHyperlinkListener(l);
        check(pane.getHyperlinkListeners().length == 0, "hyperlink listeners add/remove");

        check(JEditorPane.createEditorKitForContentType("text/plain") != null, "createEditorKitForContentType");
        check(pane.getEditorKitForContentType("text/plain") != null, "getEditorKitForContentType");
        check(pane.getPreferredSize() != null, "preferred size");
        check(pane.getUIClassID() != null, "UIClassID");

        JEditorPane typed = new JEditorPane("text/plain", "content");
        check("text/plain".equals(typed.getContentType()), "typed ctor type");
        check("content".equals(typed.getText()), "typed ctor text");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
