/*
 * @test
 * @summary javax.naming.spi.ObjectFactory conformance: white-box structure + black-box behavior.
 * @run main ObjectFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import javax.naming.CompositeName;
import javax.naming.spi.NamingManager;

public class ObjectFactoryTest {
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
        Class<?> c = Class.forName("javax.naming.spi.ObjectFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getObjectInstance", Object.class, Name.class, Context.class,
                Hashtable.class), "getObjectInstance(Object,Name,Context,Hashtable)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Anonymous implementation proves the method is callable and receives all arguments.
        final Object[] seenObj = new Object[1];
        final Name[] seenName = new Name[1];
        final Context[] seenCtx = new Context[1];
        final Hashtable<?, ?>[] seenEnv = new Hashtable<?, ?>[1];

        ObjectFactory factory = new ObjectFactory() {
            public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                           Hashtable<?, ?> environment) throws Exception {
                seenObj[0] = obj;
                seenName[0] = name;
                seenCtx[0] = nameCtx;
                seenEnv[0] = environment;
                return "created:" + obj;
            }
        };

        Hashtable<Object, Object> env = new Hashtable<>();
        env.put("k", "v");
        Object result = factory.getObjectInstance("x", null, null, env);
        check("created:x".equals(result), "getObjectInstance result");
        check("x".equals(seenObj[0]), "obj argument passed");
        check(seenEnv[0] == env, "environment argument passed");
        check(seenName[0] == null && seenCtx[0] == null, "null name/context passed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dOfDirect();
        dOfViaNamingManager();
        System.out.println("deep: ObjectFactory contracts OK");
    }

    public static class DeepOf implements ObjectFactory {
        public static Object lastObj;
        public static Name lastName;
        public static Context lastCtx;
        public static java.util.Hashtable<?, ?> lastEnv;
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            lastObj = obj; lastName = name; lastCtx = nameCtx; lastEnv = environment;
            return "OF(" + obj + ")";
        }
    }

    public static class DeepOfNull implements ObjectFactory {
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            return null;
        }
    }

    static void dOfDirect() throws Exception {
        DeepOf f = new DeepOf();
        Name n = new CompositeName("a/b");
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        check("OF(target)".equals(f.getObjectInstance("target", n, null, env)),
                "factory result");
        check(DeepOf.lastObj.equals("target"), "reference recorded");
        check(DeepOf.lastName.equals(n), "name recorded");
        check(DeepOf.lastEnv == env, "environment recorded");
        // factory may freely return null (means "not handled")
        check(new DeepOfNull().getObjectInstance("x", null, null, null) == null,
                "null return allowed");
    }

    static void dOfViaNamingManager() throws Exception {
        // OBJECT_FACTORIES property drives NamingManager lookup
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.OBJECT_FACTORIES, "ObjectFactoryTest$DeepOf");
        Name n = new CompositeName("via/nm");
        Object out = NamingManager.getObjectInstance("payload", n, null, env);
        check("OF(payload)".equals(out), "NamingManager delegates to property factory");
        check(DeepOf.lastName.equals(n), "name forwarded");
        // null-returning factory leaves the original object in place
        env = new java.util.Hashtable<>();
        env.put(Context.OBJECT_FACTORIES, "ObjectFactoryTest$DeepOfNull");
        Object sentinel = new Object();
        out = NamingManager.getObjectInstance(sentinel, null, null, env);
        check(out == sentinel, "null return -> original reference preserved");
    }

}
