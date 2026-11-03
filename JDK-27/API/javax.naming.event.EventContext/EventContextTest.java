/*
 * @test
 * @summary javax.naming.event.EventContext conformance: white-box structure + black-box behavior.
 * @run main EventContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import javax.naming.event.EventContext;
import javax.naming.event.NamingListener;

public class EventContextTest {
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
        Class<?> c = Class.forName("javax.naming.event.EventContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");

        // extends javax.naming.Context
        boolean extendsContext = false;
        for (Class<?> itf : c.getInterfaces()) {
            if (itf.getName().equals("javax.naming.Context")) extendsContext = true;
        }
        check(extendsContext, "extends javax.naming.Context");

        // method signatures
        check(hasMethod(c, "addNamingListener", javax.naming.Name.class, int.class, NamingListener.class),
                "addNamingListener(Name,int,NamingListener)");
        check(hasMethod(c, "addNamingListener", String.class, int.class, NamingListener.class),
                "addNamingListener(String,int,NamingListener)");
        check(hasMethod(c, "removeNamingListener", NamingListener.class), "removeNamingListener(NamingListener)");
        check(hasMethod(c, "targetMustExist"), "targetMustExist()");
        check(c.getMethod("targetMustExist").getReturnType() == boolean.class, "targetMustExist returns boolean");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Documented scope constant values.
        check(EventContext.OBJECT_SCOPE == 0, "OBJECT_SCOPE == 0");
        check(EventContext.ONELEVEL_SCOPE == 1, "ONELEVEL_SCOPE == 1");
        check(EventContext.SUBTREE_SCOPE == 2, "SUBTREE_SCOPE == 2");

        // A proxy implementation proves the listener-registration methods are callable.
        final boolean[] called = new boolean[2];
        EventContext ec = (EventContext) Proxy.newProxyInstance(
                EventContext.class.getClassLoader(),
                new Class<?>[]{EventContext.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "targetMustExist": return false;
                        case "addNamingListener": called[0] = true; return null;
                        case "removeNamingListener": called[1] = true; return null;
                        case "toString": return "TestEventContext";
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        default: return null;
                    }
                });

        check(!ec.targetMustExist(), "proxy targetMustExist returns false");
        NamingListener listener = evt -> { };
        ec.addNamingListener("cn=test", EventContext.SUBTREE_SCOPE, listener);
        ec.addNamingListener(new javax.naming.CompositeName("cn=test"),
                EventContext.ONELEVEL_SCOPE, listener);
        ec.removeNamingListener(listener);
        check(called[0], "addNamingListener invoked");
        check(called[1], "removeNamingListener invoked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
