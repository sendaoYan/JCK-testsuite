/*
 * @test
 * @summary javax.swing.text.StyledEditorKit.StyledTextAction conformance: white-box structure + black-box behavior.
 * @run main StyledTextActionTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

public class StyledTextActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyledEditorKit$StyledTextAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.TextAction"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static class Probe extends StyledEditorKit.StyledTextAction {
        Probe(String nm) { super(nm); }
        public void actionPerformed(ActionEvent e) {}
        public JEditorPane probeEditor(ActionEvent e) { return getEditor(e); }
        public StyledDocument probeDoc(JEditorPane e) { return getStyledDocument(e); }
        public StyledEditorKit probeKit(JEditorPane e) { return getStyledEditorKit(e); }
    }

    static void testBlackBox() {
        Probe p = new Probe("styled-probe");
        check("styled-probe".equals(p.getValue(javax.swing.Action.NAME)), "constructor sets name");
        check(p.isEnabled(), "enabled by default");

        JTextPane pane = new JTextPane();
        check(p.probeEditor(new ActionEvent(pane, ActionEvent.ACTION_PERFORMED, "x")) == pane, "getEditor resolves JTextPane");
        JTextArea area = new JTextArea();
        check(p.probeEditor(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, "x")) == null, "getEditor null for non-JEditorPane");
        check(p.probeEditor(null) == null, "getEditor null event");

        check(p.probeKit(pane) instanceof StyledEditorKit, "getStyledEditorKit type");
        check(p.probeDoc(pane) instanceof DefaultStyledDocument, "getStyledDocument type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
