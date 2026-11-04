/*
 * @test
 * @summary javax.swing.text.DefaultEditorKit.DefaultKeyTypedAction conformance: white-box structure + black-box behavior.
 * @run main DefaultKeyTypedActionTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.text.DefaultEditorKit;
import javax.swing.JTextArea;
import javax.swing.text.TextAction;

public class DefaultKeyTypedActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultEditorKit$DefaultKeyTypedAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.TextAction"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        DefaultEditorKit.DefaultKeyTypedAction a = new DefaultEditorKit.DefaultKeyTypedAction();
        check(a instanceof TextAction, "is TextAction");
        check(a instanceof Action, "is Action");
        check(DefaultEditorKit.defaultKeyTypedAction.equals(a.getValue(Action.NAME)), "name default-typed");
        check(a.isEnabled(), "enabled by default");

        JTextArea area = new JTextArea();
        area.setText("hello");

        // null event -> no-op
        a.actionPerformed(null);
        check("hello".equals(area.getText()), "null event no-op");

        // null command -> no-op (avoids toolkit path, headless-safe)
        a.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, null));
        check("hello".equals(area.getText()), "null command no-op");

        // empty command -> no-op (avoids toolkit path, headless-safe)
        a.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, ""));
        check("hello".equals(area.getText()), "empty command no-op");

        // non-text source -> target not found -> no-op
        a.actionPerformed(new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "X"));
        check("hello".equals(area.getText()), "non-text source no-op");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
