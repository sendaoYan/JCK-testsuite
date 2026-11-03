/*
 * @test
 * @summary java.security.PrivilegedAction conformance: white-box structure + black-box behavior.
 * @run main PrivilegedActionTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class PrivilegedActionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.PrivilegedAction");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"run"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Lambda implementation returning a value, run via doPrivileged
        PrivilegedAction<String> greeting = () -> "hello jck";
        check("hello jck".equals(greeting.run()), "lambda run");
        check(greeting.run().equals(greeting.run()), "repeatable");
        // Anonymous class implementation
        PrivilegedAction<Integer> counter = new PrivilegedAction<Integer>() {
            int n;
            public Integer run() { return ++n; }
        };
        check(counter.run() == 1 && counter.run() == 2, "stateful action");
        // doPrivileged executes the action exactly once per call
        List<String> calls = new ArrayList<>();
        PrivilegedAction<String> recorder = () -> { calls.add("run"); return "done"; };
        check("done".equals(AccessController.doPrivileged(recorder)), "doPrivileged executes");
        check(calls.size() == 1, "executed once");
        // Action returning null
        check(AccessController.doPrivileged((PrivilegedAction<Object>) () -> null) == null, "null return");
        // Exception propagates directly, not wrapped
        try {
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> { throw new UnsupportedOperationException("x"); });
            check(false, "propagates");
        } catch (UnsupportedOperationException uoe) {
            check("x".equals(uoe.getMessage()), "unwrapped exception");
        }
        // Used with AccessControlContext overload
        check("ctx".equals(AccessController.doPrivileged((PrivilegedAction<String>) () -> "ctx",
            AccessController.getContext())), "with context");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
