/*
 * @test
 * @summary javax.naming.event.EventDirContext conformance: white-box structure + black-box behavior.
 * @run main EventDirContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import javax.naming.event.EventDirContext;
import javax.naming.event.NamingListener;
import javax.naming.directory.SearchControls;

public class EventDirContextTest {
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
        Class<?> c = Class.forName("javax.naming.event.EventDirContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");

        boolean extEvent = false, extDir = false;
        for (Class<?> itf : c.getInterfaces()) {
            if (itf.getName().equals("javax.naming.event.EventContext")) extEvent = true;
            if (itf.getName().equals("javax.naming.directory.DirContext")) extDir = true;
        }
        check(extEvent, "extends EventContext");
        check(extDir, "extends DirContext");

        check(hasMethod(c, "addNamingListener", javax.naming.Name.class, String.class,
                SearchControls.class, NamingListener.class),
                "addNamingListener(Name,String,SearchControls,NamingListener)");
        check(hasMethod(c, "addNamingListener", String.class, String.class,
                SearchControls.class, NamingListener.class),
                "addNamingListener(String,String,SearchControls,NamingListener)");
        check(hasMethod(c, "addNamingListener", javax.naming.Name.class, String.class,
                Object[].class, SearchControls.class, NamingListener.class),
                "addNamingListener(Name,String,Object[],SearchControls,NamingListener)");
        check(hasMethod(c, "addNamingListener", String.class, String.class,
                Object[].class, SearchControls.class, NamingListener.class),
                "addNamingListener(String,String,Object[],SearchControls,NamingListener)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Scope constants are inherited from EventContext (compile-time constants).
        check(EventDirContext.OBJECT_SCOPE == 0, "OBJECT_SCOPE == 0");
        check(EventDirContext.SUBTREE_SCOPE == 2, "SUBTREE_SCOPE == 2");

        // Proxy proves the search-scoped listener-registration overloads are callable.
        final int[] addCalls = new int[1];
        EventDirContext edc = (EventDirContext) Proxy.newProxyInstance(
                EventDirContext.class.getClassLoader(),
                new Class<?>[]{EventDirContext.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "addNamingListener": addCalls[0]++; return null;
                        case "targetMustExist": return false;
                        case "toString": return "TestEventDirContext";
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        default: return null;
                    }
                });

        NamingListener listener = evt -> { };
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        edc.addNamingListener("cn=test", "(objectClass=*)", ctls, listener);
        edc.addNamingListener(new javax.naming.CompositeName("cn=test"), "(uid=*)",
                new Object[]{"java.lang.String"}, ctls, listener);
        check(addCalls[0] == 2, "both search-scoped addNamingListener overloads invoked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
