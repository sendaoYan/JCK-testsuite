/*
 * @test
 * @summary javax.naming.PartialResultException conformance: white-box structure + black-box behavior.
 * @run main PartialResultExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.PartialResultException;

public class PartialResultExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = javax.naming.PartialResultException.class;
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
        PartialResultException withMsg = new PartialResultException("reason");
        check(withMsg.getMessage().equals("reason"), "message preserved");
        PartialResultException noMsg = new PartialResultException();
        check(noMsg.getMessage() == null, "no-arg message null");
        withMsg.setRemainingName(new CompositeName("a/b"));
        check(withMsg.getRemainingName().toString().equals("a/b"), "remaining name round trip");
        withMsg.setResolvedObj("obj");
        check("obj".equals(withMsg.getResolvedObj()), "resolved obj");
        String ts = withMsg.toString();
        check(ts.contains("javax.naming.PartialResultException") && ts.contains("reason"), "toString naming");
        javax.naming.PartialResultException pre = new javax.naming.PartialResultException("partial results only");
        pre.appendRemainingComponent("next1");
        pre.appendRemainingComponent("next2");
        check(pre.getRemainingName().toString().equals("next1/next2"), "append remaining chain");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
