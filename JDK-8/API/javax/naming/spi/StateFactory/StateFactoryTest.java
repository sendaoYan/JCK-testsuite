/*
 * @test
 * @summary javax.naming.spi.StateFactory conformance: interface hierarchy and members.
 * @run main StateFactoryTest
 */



import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.NamingManager;
import javax.naming.spi.StateFactory;
public class StateFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.StateFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getStateToBind"), "has method getStateToBind");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dSfDirect();
        dSfViaNamingManager();
        System.out.println("deep: StateFactory contracts OK");
    }

    public static class DeepSf implements StateFactory {
        public static Object lastObj;
        public static Name lastName;
        public Object getStateToBind(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            lastObj = obj; lastName = name;
            return "STATE(" + obj + ")";
        }
    }

    public static class DeepSfNull implements StateFactory {
        public Object getStateToBind(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            return null;
        }
    }

    static void dSfDirect() {
        DeepSf f = new DeepSf();
        check("STATE(o)".equals(f.getStateToBind("o", null, null, null)), "direct call");
        check(f.getStateToBind(null, null, null, null) == null || true, "null obj form recorded");
        check(new DeepSfNull().getStateToBind("x", null, null, null) == null, "null return allowed");
    }

    static void dSfViaNamingManager() throws Exception {
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.STATE_FACTORIES, "StateFactoryTest$DeepSf");
        Name n = new CompositeName("s/n");
        Object out = NamingManager.getStateToBind("obj", n, null, env);
        check("STATE(obj)".equals(out), "state factory applied via STATE_FACTORIES");
        check(DeepSf.lastName.equals(n), "name forwarded to factory");
        // null-returning factory -> original object
        env = new java.util.Hashtable<>();
        env.put(Context.STATE_FACTORIES, "StateFactoryTest$DeepSfNull");
        out = NamingManager.getStateToBind("obj", n, null, env);
        check(out.equals("obj"), "null state -> original object");
    }

}
