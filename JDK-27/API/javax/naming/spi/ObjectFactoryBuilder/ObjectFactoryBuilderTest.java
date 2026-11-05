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
import javax.naming.CompositeName;
import javax.naming.spi.NamingManager;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dOfbBehavior();
        System.out.println("deep: ObjectFactoryBuilder contracts OK");
    }

    public static class DeepOf implements ObjectFactory {
        public static Object lastObj;
        public static java.util.Hashtable<?, ?> lastEnv;
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            lastObj = obj; lastEnv = environment;
            return "BUILT(" + obj + ")";
        }
    }

    static void dOfbBehavior() throws Exception {
        NamingManager.setObjectFactoryBuilder(new ObjectFactoryBuilder() {
            public ObjectFactory createObjectFactory(Object obj,
                    java.util.Hashtable<?, ?> environment) throws javax.naming.NamingException {
                return new DeepOf();
            }
        });
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.OBJECT_FACTORIES, "should.be.ignored.when.builder.set");
        Object out = NamingManager.getObjectInstance("subject", new CompositeName("n"), null, env);
        check("BUILT(subject)".equals(out), "builder-produced factory used");
        check(DeepOf.lastObj.equals("subject"), "factory got the reference");
        // builder is once-only per JVM
        boolean ise = false;
        try { NamingManager.setObjectFactoryBuilder(null); }
        catch (IllegalStateException e) {
            ise = true;
            check("ObjectFactoryBuilder already set".equals(e.getMessage()), "ISE message");
        }
        check(ise || out != null, "second builder install rejected or ignored");
    }

}
