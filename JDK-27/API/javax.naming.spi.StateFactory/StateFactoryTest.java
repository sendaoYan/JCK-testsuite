/*
 * @test
 * @summary javax.naming.spi.StateFactory conformance: white-box structure + black-box behavior.
 * @run main StateFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.StateFactory;

public class StateFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.StateFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getStateToBind", Object.class, Name.class, Context.class,
                Hashtable.class), "getStateToBind(Object,Name,Context,Hashtable)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final Object[] seenObj = new Object[1];
        final Name[] seenName = new Name[1];
        final Hashtable<?, ?>[] seenEnv = new Hashtable<?, ?>[1];

        StateFactory factory = new StateFactory() {
            public Object getStateToBind(Object obj, Name name, Context nameCtx,
                                        Hashtable<?, ?> environment) {
                seenObj[0] = obj;
                seenName[0] = name;
                seenEnv[0] = environment;
                return "state:" + obj;
            }
        };

        Hashtable<Object, Object> env = new Hashtable<>();
        env.put("k", "v");
        Object result = factory.getStateToBind("x", null, null, env);
        check("state:x".equals(result), "getStateToBind result");
        check("x".equals(seenObj[0]), "obj argument passed");
        check(seenEnv[0] == env, "environment argument passed");
        check(seenName[0] == null, "null name passed");

        // Returning null is a valid "no transformation" response.
        StateFactory identity = (obj, name, ctx, environment) -> null;
        check(identity.getStateToBind("x", null, null, null) == null,
                "null return is a valid response");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
