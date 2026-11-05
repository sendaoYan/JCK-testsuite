/*
 * @test
 * @summary javax.naming.spi.ObjectFactory conformance: interface hierarchy and members.
 * @run main ObjectFactoryTest
 */



import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.NamingManager;
import javax.naming.spi.ObjectFactory;
public class ObjectFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.ObjectFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getObjectInstance"), "has method getObjectInstance");        testDeepContracts();
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
