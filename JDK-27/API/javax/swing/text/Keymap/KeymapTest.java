/*
 * @test
 * @summary javax.swing.text.Keymap conformance: white-box structure + black-box behavior.
 * @run main KeymapTest
 */

import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

public class KeymapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Keymap");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static boolean containsKeyStroke(KeyStroke[] arr, KeyStroke k) {
        for (KeyStroke x : arr) if (k.equals(x)) return true;
        return false;
    }

    static boolean containsAction(Action[] arr, Action a) {
        for (Action x : arr) if (x == a) return true;
        return false;
    }

    static void testBlackBox() {
        Action act = new DefaultEditorKit.InsertTabAction();
        Action act2 = new DefaultEditorKit.InsertBreakAction();
        KeyStroke ks = KeyStroke.getKeyStroke('A');
        KeyStroke ks2 = KeyStroke.getKeyStroke('B');

        Keymap km = JTextComponent.addKeymap("jck-km-test", null);
        check(km != null, "addKeymap returns keymap");
        check("jck-km-test".equals(km.getName()), "keymap name");
        check(km.getResolveParent() == null, "resolve parent null initially");
        check(km.getDefaultAction() == null, "default action null initially");
        km.setDefaultAction(act);
        check(km.getDefaultAction() == act, "default action roundtrip");

        km.addActionForKeyStroke(ks, act);
        check(km.getAction(ks) == act, "getAction bound");
        check(km.isLocallyDefined(ks), "locally defined");
        KeyStroke[] bound = km.getBoundKeyStrokes();
        check(bound.length == 1 && containsKeyStroke(bound, ks), "bound keystrokes");
        Action[] boundActs = km.getBoundActions();
        check(boundActs.length == 1 && containsAction(boundActs, act), "bound actions");
        KeyStroke[] forAction = km.getKeyStrokesForAction(act);
        check(forAction != null && containsKeyStroke(forAction, ks), "keystrokes for action");

        km.removeKeyStrokeBinding(ks);
        check(km.getAction(ks) == null, "binding removed");
        check(!km.isLocallyDefined(ks), "not locally defined after remove");
        km.addActionForKeyStroke(ks, act);
        km.removeBindings();
        check(km.getBoundKeyStrokes().length == 0, "removeBindings clears");

        // resolve parent lookup
        Keymap parent = JTextComponent.addKeymap("jck-km-parent", null);
        Keymap child = JTextComponent.addKeymap("jck-km-child", parent);
        check(child.getResolveParent() == parent, "child resolve parent");
        parent.addActionForKeyStroke(ks2, act2);
        check(child.getAction(ks2) == act2, "child inherits parent binding");
        check(!child.isLocallyDefined(ks2), "child not locally defined for parent binding");
        check(parent.isLocallyDefined(ks2), "parent locally defined");
        KeyStroke[] childForAct2 = child.getKeyStrokesForAction(act2);
        check(childForAct2 != null && containsKeyStroke(childForAct2, ks2), "child keystrokes for parent action");

        // named keymap registry
        check(JTextComponent.getKeymap("jck-km-test") == km, "getKeymap registry");
        check(JTextComponent.removeKeymap("jck-km-test") == km, "removeKeymap returns");
        check(JTextComponent.getKeymap("jck-km-test") == null, "removed from registry");

        // JTextComponent keymap property
        JTextArea area = new JTextArea();
        Keymap def = area.getKeymap();
        check(def != null, "default keymap non-null");
        Keymap custom = JTextComponent.addKeymap("jck-km-custom", null);
        area.setKeymap(custom);
        check(area.getKeymap() == custom, "setKeymap roundtrip");

        // loadKeymap binds named actions
        Keymap loaded = JTextComponent.addKeymap("jck-km-load", null);
        JTextComponent.KeyBinding[] bindings = { new JTextComponent.KeyBinding(ks, DefaultEditorKit.insertTabAction) };
        Action[] actions = { act };
        JTextComponent.loadKeymap(loaded, bindings, actions);
        check(loaded.getAction(ks) == act, "loadKeymap binds");
        check(loaded.isLocallyDefined(ks), "loadKeymap locally defined");

        // cleanup named registry entries
        JTextComponent.removeKeymap("jck-km-parent");
        JTextComponent.removeKeymap("jck-km-child");
        JTextComponent.removeKeymap("jck-km-custom");
        JTextComponent.removeKeymap("jck-km-load");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
