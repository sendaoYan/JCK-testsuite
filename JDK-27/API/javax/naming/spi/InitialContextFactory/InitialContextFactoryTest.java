/*
 * @test
 * @summary javax.naming.spi.InitialContextFactory conformance: white-box structure + black-box behavior.
 * @run main InitialContextFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;

public class InitialContextFactoryTest {
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
        Class<?> c = Class.forName("javax.naming.spi.InitialContextFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getInitialContext", Hashtable.class), "getInitialContext(Hashtable)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Anonymous implementation proves the single method is callable and receives the env.
        final Hashtable<?, ?>[] seen = new Hashtable<?, ?>[1];
        InitialContextFactory factory = env -> {
            seen[0] = env;
            return null;
        };

        Hashtable<Object, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.example.Factory");
        Context ctx = factory.getInitialContext(env);
        check(seen[0] == env, "getInitialContext receives the environment");
        check(ctx == null, "null context is returned as-is");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
