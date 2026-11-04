/*
 * @test
 * @summary javax.naming.NamingException conformance: white-box structure + black-box behavior.
 * @run main NamingExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;

public class NamingExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = NamingException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        for (String m : new String[] {"getResolvedName", "getRemainingName", "getResolvedObj",
                "setResolvedName", "setRemainingName", "setResolvedObj", "appendRemainingComponent",
                "appendRemainingName", "getRootCause", "setRootCause", "getCause", "initCause",
                "getExplanation", "toString"})
            check(method(c, m), "declares " + m);
        check(c.getMethod("toString", boolean.class) != null, "toString(boolean)");
        Constructor<?>[] cs = c.getDeclaredConstructors();
        boolean noArg = false, strArg = false;
        for (Constructor<?> k : cs) {
            if (k.getParameterCount() == 0) noArg = true;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) strArg = true;
        }
        check(noArg && strArg, "both ctors");
    }
    static boolean method(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        NamingException ne = new NamingException("boom");
        check(ne.getMessage().equals("boom"), "message");
        check(ne.getExplanation().equals("boom"), "explanation == message");
        check(new NamingException().getExplanation() == null, "null explanation");

        // resolved/remaining name and object plumbing
        Name src = new CompositeName("a/b");
        ne.setRemainingName(src);
        check(ne.getRemainingName().toString().equals("a/b"), "remaining set");
        check(ne.getRemainingName() != src, "remaining name is a clone");
        ne.setResolvedName(new CompositeName("r"));
        check(ne.getResolvedName().get(0).equals("r"), "resolved set");
        ne.setResolvedObj("o");
        check("o".equals(ne.getResolvedObj()), "resolved obj");
        ne.setRemainingName(null);
        check(ne.getRemainingName() == null, "null remaining clears");

        // append chains
        NamingException ap = new NamingException();
        ap.appendRemainingComponent("x");
        ap.appendRemainingComponent("y");
        check(ap.getRemainingName().toString().equals("x/y"), "appendRemainingComponent");
        ap.appendRemainingName(new CompositeName("p/q"));
        check(ap.getRemainingName().toString().equals("x/y/p/q"), "appendRemainingName");

        // root cause interplay with Throwable cause
        RuntimeException rc = new RuntimeException("rc");
        ne.setRootCause(rc);
        check(ne.getRootCause() == rc, "rootCause stored");
        check(ne.getCause() == rc, "cause mirrors rootCause");
        NamingException ic = new NamingException("i");
        ic.initCause(rc);
        check(ic.getRootCause() == rc && ic.getCause() == rc, "initCause sets rootCause");
        try { ic.initCause(new RuntimeException("second")); throw new AssertionError("double initCause"); }
        catch (IllegalStateException expected) { check(true, "initCause twice throws ISE"); }
        check(new NamingException().getRootCause() == null, "no rootCause by default");

        // toString composition
        NamingException t = new NamingException("m");
        t.setRemainingName(new CompositeName("a/b"));
        t.setRootCause(rc);
        String s = t.toString();
        check(s.contains("javax.naming.NamingException: m"), "toString header");
        check(s.contains("[Root exception is java.lang.RuntimeException: rc]"), "toString root exception");
        check(s.contains("remaining name 'a/b'"), "toString remaining name");
        String brief = t.toString(false);
        check(brief.contains("m"), "toString(false) keeps message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
