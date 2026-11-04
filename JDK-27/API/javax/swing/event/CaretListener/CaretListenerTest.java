/*
 * @test
 * @summary javax.swing.event.CaretListener conformance: white-box structure + black-box behavior.
 * @run main CaretListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class CaretListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.CaretListener");
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
        Method m = c.getMethod("caretUpdate", CaretEvent.class);
        check(m.getReturnType() == void.class, "caretUpdate returns void");
        check(Modifier.isAbstract(m.getModifiers()), "caretUpdate abstract");
        check(c.getDeclaredMethods().length == 1, "declared method count == 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final CaretEvent[] last = new CaretEvent[1];
        CaretListener l = new CaretListener() {
            public void caretUpdate(CaretEvent e) { last[0] = e; }
        };

        final Object src = new Object();
        CaretEvent ev = new CaretEvent(src) {
            public int getDot() { return 3; }
            public int getMark() { return 5; }
        };
        l.caretUpdate(ev);
        check(last[0] == ev, "caretUpdate fired");
        check(last[0].getSource() == src, "getSource round-trip");
        check(last[0].getDot() == 3, "getDot round-trip");
        check(last[0].getMark() == 5, "getMark round-trip");
        check(ev.getDot() != ev.getMark(), "dot and mark independent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
