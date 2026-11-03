/*
 * @test
 * @summary javax.naming.CannotProceedException conformance: white-box structure + black-box behavior.
 * @run main CannotProceedExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.CannotProceedException;

public class CannotProceedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = javax.naming.CannotProceedException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == javax.naming.NamingException.class, "extends NamingException");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        Constructor<?>[] cs = c.getDeclaredConstructors();
        boolean noArg = false, strArg = false;
        for (Constructor<?> k : cs) {
            if (k.getParameterCount() == 0) noArg = true;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) strArg = true;
        }
        check(noArg, "has no-arg ctor");
        check(strArg, "has String ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CannotProceedException withMsg = new CannotProceedException("reason");
        check(withMsg.getMessage().equals("reason"), "message preserved");
        CannotProceedException noMsg = new CannotProceedException();
        check(noMsg.getMessage() == null, "no-arg message null");
        withMsg.setRemainingName(new CompositeName("a/b"));
        check(withMsg.getRemainingName().toString().equals("a/b"), "remaining name round trip");
        withMsg.setResolvedObj("obj");
        check("obj".equals(withMsg.getResolvedObj()), "resolved obj");
        String ts = withMsg.toString();
        check(ts.contains("javax.naming.CannotProceedException") && ts.contains("reason"), "toString naming");
        javax.naming.CannotProceedException cpe = new javax.naming.CannotProceedException("cannot continue at boundary");
        java.util.Hashtable<String, Object> env = new java.util.Hashtable<>();
        cpe.setEnvironment(env);
        check(cpe.getEnvironment() == env, "environment reference kept");
        javax.naming.Name alt = new CompositeName("alt");
        cpe.setAltName(alt);
        check(cpe.getAltName().toString().equals("alt"), "alt name");
        cpe.setAltNameCtx(null);
        check(cpe.getAltNameCtx() == null, "alt ctx default null");
        check(cpe instanceof NamingException, "NamingException hierarchy");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
