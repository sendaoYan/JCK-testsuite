/*
 * @test
 * @summary javax.swing.event.ChangeListener conformance: white-box structure + black-box behavior.
 * @run main ChangeListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ChangeListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.ChangeListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "interface 0");
        Method m = c.getMethod("stateChanged", ChangeEvent.class);
        check(m.getReturnType() == void.class, "stateChanged returns void");
        check(Modifier.isAbstract(m.getModifiers()), "stateChanged abstract");
        check(c.getDeclaredMethods().length == 1, "declared method count == 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final ChangeEvent[] last = new ChangeEvent[1];
        ChangeListener l = new ChangeListener() {
            public void stateChanged(ChangeEvent e) { last[0] = e; }
        };
        Object src = new Object();
        ChangeEvent ev = new ChangeEvent(src);
        l.stateChanged(ev);
        check(last[0] == ev, "stateChanged fired");
        check(last[0].getSource() == src, "getSource round-trip");
        ChangeEvent ev2 = new ChangeEvent(src);
        l.stateChanged(ev2);
        check(last[0] == ev2, "stateChanged fired again (re-entrant)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
