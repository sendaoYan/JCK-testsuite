/*
 * @test
 * @summary javax.swing.text.JTextComponent.KeyBinding conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main KeyBindingTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

public class KeyBindingTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.JTextComponent$KeyBinding");
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
        check(c.getConstructor(KeyStroke.class, String.class) != null, "ctor(KeyStroke,String) present");
        check(c.getField("key").getType() == KeyStroke.class, "public field key");
        check(c.getField("actionName").getType() == String.class, "public field actionName");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        KeyStroke ksA = KeyStroke.getKeyStroke('A');
        JTextComponent.KeyBinding kb = new JTextComponent.KeyBinding(ksA, DefaultEditorKit.cutAction);
        check(kb.key == ksA, "key field round trip");
        check(ksA.equals(kb.key), "key equals round trip");
        check(DefaultEditorKit.cutAction.equals(kb.actionName), "actionName field round trip");

        KeyStroke ctrlB = KeyStroke.getKeyStroke("control B");
        JTextComponent.KeyBinding kb2 = new JTextComponent.KeyBinding(ctrlB, DefaultEditorKit.copyAction);
        check(ctrlB.equals(kb2.key), "keystroke from string round trip");
        check(DefaultEditorKit.copyAction.equals(kb2.actionName), "actionName 2");

        // canonical use: loadKeymap pairs each KeyBinding with a named Action
        Keymap km = JTextComponent.addKeymap("jkb-test", null);
        Action cut = new DefaultEditorKit.CutAction();
        Action copy = new DefaultEditorKit.CopyAction();
        JTextComponent.loadKeymap(km, new JTextComponent.KeyBinding[]{kb, kb2}, new Action[]{cut, copy});
        check(km.getAction(ksA) == cut, "loadKeymap binds cut action");
        check(km.getAction(ctrlB) == copy, "loadKeymap binds copy action");
        check(km.isLocallyDefined(ksA) && km.isLocallyDefined(ctrlB), "bindings locally defined");
        JTextComponent.removeKeymap("jkb-test");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(KeyBindingTest::testWhiteBox);
        runOnEDT(KeyBindingTest::testBlackBox);
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
