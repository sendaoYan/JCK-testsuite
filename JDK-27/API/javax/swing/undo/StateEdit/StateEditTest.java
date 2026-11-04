/*
 * @test
 * @summary javax.swing.undo.StateEdit conformance: white-box structure + black-box behavior.
 * @run main StateEditTest
 */

import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.swing.undo.StateEdit;
import javax.swing.undo.StateEditable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class StateEditTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // A simple mutable object that can store/restore its state.
    static class Model implements StateEditable {
        String value;
        int count;
        Model(String value, int count) { this.value = value; this.count = count; }
        public void storeState(Hashtable<Object, Object> state) {
            state.put("value", value);
            state.put("count", Integer.valueOf(count));
        }
        @SuppressWarnings("unchecked")
        public void restoreState(Hashtable<?, ?> state) {
            Hashtable<Object, Object> s = (Hashtable<Object, Object>) state;
            value = (String) s.get("value");
            count = ((Integer) s.get("count")).intValue();
        }
    }

    // Exposes protected state tables so redundant-state removal can be observed.
    static class ExposedStateEdit extends StateEdit {
        ExposedStateEdit(StateEditable o, String name) { super(o, name); }
        Hashtable<Object, Object> pre() { return preState; }
        Hashtable<Object, Object> post() { return postState; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.undo.StateEdit");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.undo.AbstractUndoableEdit"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Named construction + presentation name.
        Model m = new Model("before", 1);
        StateEdit named = new StateEdit(m, "Edit value");
        check("Edit value".equals(named.getPresentationName()), "named presentation name");
        check(named.canUndo(), "fresh state edit can undo");

        // Round-trip: store -> mutate -> end -> undo -> redo.
        m.value = "after";
        m.count = 2;
        named.end();
        named.undo();
        check("before".equals(m.value) && m.count == 1, "undo restores pre-state");
        check(named.canRedo(), "after undo can redo");
        named.redo();
        check("after".equals(m.value) && m.count == 2, "redo restores post-state");
        check(named.canUndo(), "after redo can undo again");

        // Unnamed constructor leaves presentation name null.
        Model m2 = new Model("x", 0);
        StateEdit unnamed = new StateEdit(m2);
        check(unnamed.getPresentationName() == null, "unnamed presentation name null");

        // Redundant state (unchanged keys) is removed by end().
        Model m3 = new Model("same", 7);
        ExposedStateEdit exposed = new ExposedStateEdit(m3, "noop");
        m3.value = "same";   // unchanged
        m3.count = 8;        // changed
        exposed.end();
        check(!exposed.pre().containsKey("value") && !exposed.post().containsKey("value"),
                "redundant unchanged key removed");
        check(exposed.pre().containsKey("count") && exposed.post().containsKey("count"),
                "changed key retained");
        check(exposed.pre().get("count").equals(Integer.valueOf(7)), "pre-state keeps original value");
        check(exposed.post().get("count").equals(Integer.valueOf(8)), "post-state keeps new value");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.undo.StateEdit", false, StateEditTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.undo.AbstractUndoableEdit"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("end"), "method end");
        check(names.contains("undo"), "method undo");
        check(names.contains("redo"), "method redo");
        check(names.contains("getPresentationName"), "method getPresentationName");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
