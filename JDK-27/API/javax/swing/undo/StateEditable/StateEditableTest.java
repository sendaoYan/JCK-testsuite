/*
 * @test
 * @summary javax.swing.undo.StateEditable conformance: white-box structure + black-box behavior.
 * @run main StateEditableTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.swing.undo.StateEditable;

public class StateEditableTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // A concrete implementation used to exercise the interface contract.
    static class Model implements StateEditable {
        String value;
        Model(String v) { this.value = v; }
        public void storeState(Hashtable<Object, Object> state) {
            state.put("value", value);
        }
        @SuppressWarnings("unchecked")
        public void restoreState(Hashtable<?, ?> state) {
            this.value = (String) ((Hashtable<Object, Object>) state).get("value");
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.undo.StateEditable");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getDeclaredMethods().length == 2, "two methods: storeState + restoreState");
        Method store = c.getMethod("storeState", Hashtable.class);
        Method restore = c.getMethod("restoreState", Hashtable.class);
        check(store != null && restore != null, "storeState/restoreState present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Model m = new Model("original");

        // storeState captures current state into the supplied table.
        Hashtable<Object, Object> snapshot = new Hashtable<Object, Object>();
        m.storeState(snapshot);
        check("original".equals(snapshot.get("value")), "storeState captures state");

        // Mutate, then restoreState reapplies the captured state.
        m.value = "mutated";
        m.restoreState(snapshot);
        check("original".equals(m.value), "restoreState reapplies captured state");

        // A fresh snapshot after mutation reflects the new state (round-trip via interface).
        StateEditable asInterface = m;
        Hashtable<Object, Object> snapshot2 = new Hashtable<Object, Object>();
        m.value = "mutated";
        asInterface.storeState(snapshot2);   // captures "mutated"
        m.value = "again";
        asInterface.restoreState(snapshot2); // back to "mutated"
        check("mutated".equals(m.value), "interface round-trip works");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
