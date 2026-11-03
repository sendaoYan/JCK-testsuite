/*
 * @test
 * @summary javax.swing.text.html.AccessibleHTML.TextElementInfo.TextAccessibleContext conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TextAccessibleContextTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleText;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class TextAccessibleContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.AccessibleHTML$TextElementInfo$TextAccessibleContext");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.AccessibleHTML$HTMLAccessibleContext"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.AccessibleText"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JFrame frame = new JFrame();
        JEditorPane editor = new JEditorPane();
        try {
            editor.setEditorKit(new HTMLEditorKit());
            editor.setLocale(Locale.US);
            editor.setText("<html><body><p>hello world</p></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            Element p = doc.getElement(doc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.P);
            check(p != null, "P element found");
            Element content = doc.getElement(p, StyleConstants.NameAttribute, HTML.Tag.CONTENT);
            check(content != null, "CONTENT element found");
            int start = content.getStartOffset();
            int len = content.getEndOffset() - content.getStartOffset();
            check(len > 0, "content non-empty");

            HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
            List<AccessibleContext> texts = new ArrayList<AccessibleContext>();
            collectTexts(kit.getAccessibleContext(), texts);
            check(texts.size() >= 1, "at least one TextAccessibleContext");

            AccessibleContext text = null;
            for (AccessibleContext t : texts) {
                AccessibleText at = (AccessibleText) t;
                if (at.getCharCount() == len && at.getCaretPosition() == editor.getCaretPosition()) {
                    text = t;
                    break;
                }
            }
            check(text != null, "TextAccessibleContext for 'hello world' reachable");
            check(Class.forName("javax.swing.text.html.AccessibleHTML$TextElementInfo$TextAccessibleContext").isInstance(text), "instanceof TextAccessibleContext");
            check(text instanceof AccessibleText, "is AccessibleText");

            check(text.getAccessibleRole() == AccessibleRole.TEXT, "role TEXT");
            check(text.getAccessibleName() == null, "name null without title");
            check("text/html".equals(text.getAccessibleDescription()), "description == content type");

            AccessibleText at = (AccessibleText) text;
            check(at.getCharCount() == len, "charCount == element span");
            check(at.getCaretPosition() == editor.getCaretPosition(), "caret delegates to editor");

            check(at.getSelectionStart() == editor.getSelectionStart(), "selectionStart delegates");
            check(at.getSelectionEnd() == editor.getSelectionEnd(), "selectionEnd delegates");
            check(at.getSelectedText() == null, "no selection -> null selectedText");

            check(at.getCharacterAttribute(start) != null, "characterAttribute non-null");

            check("h".equals(at.getAtIndex(AccessibleText.CHARACTER, start)), "getAtIndex(CHAR, start) == 'h'");
            check("e".equals(at.getAtIndex(AccessibleText.CHARACTER, start + 1)), "getAtIndex(CHAR, start+1) == 'e'");
            check(at.getAtIndex(AccessibleText.CHARACTER, doc.getLength()) == null, "getAtIndex out of range -> null");
            check(at.getAtIndex(AccessibleText.CHARACTER, -1) == null, "getAtIndex negative -> null");

            // getAccessibleText() is a class-level method (not on AccessibleText)
            Method m = text.getClass().getMethod("getAccessibleText");
            check(m.invoke(text) == text, "getAccessibleText() == this");
        } finally {
            frame.dispose();
        }
    }

    static void collectTexts(AccessibleContext c, List<AccessibleContext> out) {
        if (c.getClass().getName().endsWith("TextAccessibleContext")) out.add(c);
        int n = c.getAccessibleChildrenCount();
        for (int i = 0; i < n; i++) {
            Accessible a = c.getAccessibleChild(i);
            if (a != null && a.getAccessibleContext() != null) {
                collectTexts(a.getAccessibleContext(), out);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(TextAccessibleContextTest::testWhiteBox);
        runOnEDT(TextAccessibleContextTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}
