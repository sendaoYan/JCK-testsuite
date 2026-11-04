/*
 * @test
 * @summary java.util.EventObject conformance: white-box structure + black-box behavior.
 * @run main EventObjectTest
 */

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.EventObject;

public class EventObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.EventObject");
        check(!Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()),
                "concrete, non-final");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        Constructor<?> ctor = c.getConstructor(Object.class);
        check(Modifier.isPublic(ctor.getModifiers()), "public EventObject(Object)");
        check(c.getMethod("getSource").getReturnType() == Object.class, "getSource -> Object");
        check(c.getMethod("toString").getReturnType() == String.class, "toString -> String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. getSource returns the constructor argument by identity
        Object src = new Object();
        EventObject e = new EventObject(src);
        check(e.getSource() == src, "getSource returns the constructor argument");

        // 2. null source is rejected
        expectThrows(() -> new EventObject(null), IllegalArgumentException.class,
                "null source rejected");

        // 3. toString includes the class name and the source
        String s = e.toString();
        check(s.contains("EventObject") && s.contains("source="), "toString includes class and source");
        check(s.contains("java.lang.Object@") || s.contains(src.toString()),
                "toString renders the source");

        // 4. subclassing for a typed event source
        class Button {
            final String id = "b1";
        }
        class ActionEvent extends EventObject {
            ActionEvent(Object source) { super(source); }
        }
        Button btn = new Button();
        ActionEvent ae = new ActionEvent(btn);
        check(ae.getSource() == btn, "subclass inherits getSource");
        check(ae.toString().contains("ActionEvent"), "subclass toString names its own class");

        // 5. distinct events with the same source are distinct objects
        EventObject a1 = new EventObject(src);
        EventObject a2 = new EventObject(src);
        check(a1 != a2, "distinct events are distinct objects");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
