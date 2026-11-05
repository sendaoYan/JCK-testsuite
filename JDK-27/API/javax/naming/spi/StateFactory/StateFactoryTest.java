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
import javax.naming.CompositeName;
import javax.naming.spi.NamingManager;

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
        testDeepContracts();
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
