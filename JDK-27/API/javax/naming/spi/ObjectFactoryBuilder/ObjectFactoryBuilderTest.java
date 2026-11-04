/*
 * @test
 * @summary javax.naming.spi.ObjectFactoryBuilder conformance: white-box structure + black-box behavior.
 * @run main ObjectFactoryBuilderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.ObjectFactoryBuilder;

public class ObjectFactoryBuilderTest {
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
        Class<?> c = Class.forName("javax.naming.spi.ObjectFactoryBuilder");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "createObjectFactory", Object.class, Hashtable.class),
                "createObjectFactory(Object,Hashtable)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final Object[] seenObj = new Object[1];
        final Hashtable<?, ?>[] seenEnv = new Hashtable<?, ?>[1];

        ObjectFactoryBuilder builder = (obj, environment) -> {
            seenObj[0] = obj;
            seenEnv[0] = environment;
            return new ObjectFactory() {
                public Object getObjectInstance(Object o, Name name, Context nameCtx,
                                               Hashtable<?, ?> env) throws Exception {
                    return "factory-made:" + o;
                }
            };
        };

        Hashtable<Object, Object> env = new Hashtable<>();
        env.put("k", "v");
        ObjectFactory factory = builder.createObjectFactory("x", env);
        check(seenObj[0] == "x", "builder receives object argument");
        check(seenEnv[0] == env, "builder receives environment");
        check(factory != null, "builder returns a factory");

        // The produced factory is itself callable.
        Object result = factory.getObjectInstance("y", null, null, null);
        check("factory-made:y".equals(result), "produced factory is callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
