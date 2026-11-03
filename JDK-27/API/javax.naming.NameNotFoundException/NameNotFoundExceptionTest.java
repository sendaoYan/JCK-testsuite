/*
 * @test
 * @summary javax.naming.NameNotFoundException conformance: white-box structure + black-box behavior.
 * @run main NameNotFoundExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;

public class NameNotFoundExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = javax.naming.NameNotFoundException.class;
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
        NameNotFoundException withMsg = new NameNotFoundException("reason");
        check(withMsg.getMessage().equals("reason"), "message preserved");
        NameNotFoundException noMsg = new NameNotFoundException();
        check(noMsg.getMessage() == null, "no-arg message null");
        withMsg.setRemainingName(new CompositeName("a/b"));
        check(withMsg.getRemainingName().toString().equals("a/b"), "remaining name round trip");
        withMsg.setResolvedObj("obj");
        check("obj".equals(withMsg.getResolvedObj()), "resolved obj");
        String ts = withMsg.toString();
        check(ts.contains("javax.naming.NameNotFoundException") && ts.contains("reason"), "toString naming");
        // typical producer: LdapCtx lookup miss reports remaining name
        javax.naming.NameNotFoundException nnfe = new javax.naming.NameNotFoundException("unable to find: cn=missing");
        nnfe.setRemainingName(new CompositeName("cn=missing"));
        check(nnfe.getRemainingName().get(0).equals("cn=missing"), "remaining comp");
        check(nnfe.getExplanation().equals("unable to find: cn=missing"), "explanation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
