/*
 * @test
 * @summary javax.naming.spi.InitialContextFactoryBuilder conformance: white-box structure + black-box behavior.
 * @run main InitialContextFactoryBuilderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

public class InitialContextFactoryBuilderTest {
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
        Class<?> c = Class.forName("javax.naming.spi.InitialContextFactoryBuilder");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "createInitialContextFactory", Hashtable.class),
                "createInitialContextFactory(Hashtable)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final Hashtable<?, ?>[] seen = new Hashtable<?, ?>[1];
        final InitialContextFactory produced = env -> null;

        // Anonymous implementation: builder returns a factory, which is then used.
        InitialContextFactoryBuilder builder = env -> {
            seen[0] = env;
            return produced;
        };

        Hashtable<Object, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.example.Factory");
        InitialContextFactory factory = builder.createInitialContextFactory(env);
        check(seen[0] == env, "builder receives the environment");
        check(factory == produced, "builder returns the factory it produced");
        check(factory.getInitialContext(env) == null, "produced factory is callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
