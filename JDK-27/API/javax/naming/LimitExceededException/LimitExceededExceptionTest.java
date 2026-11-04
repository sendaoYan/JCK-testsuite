/*
 * @test
 * @summary javax.naming.LimitExceededException conformance: white-box structure + black-box behavior.
 * @run main LimitExceededExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.LimitExceededException;

public class LimitExceededExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = javax.naming.LimitExceededException.class;
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
        LimitExceededException withMsg = new LimitExceededException("reason");
        check(withMsg.getMessage().equals("reason"), "message preserved");
        check(withMsg.getExplanation().equals("reason"), "explanation == message");
        LimitExceededException noMsg = new LimitExceededException();
        check(noMsg.getMessage() == null, "no-arg message null");

        // inherited NamingException plumbing used by every subclass
        withMsg.setRemainingName(new CompositeName("a/b"));
        check(withMsg.getRemainingName().toString().equals("a/b"), "remaining name round trip");
        withMsg.setResolvedName(new CompositeName("r"));
        check(withMsg.getResolvedName().size() == 1, "resolved name");
        withMsg.setResolvedObj("obj");
        check("obj".equals(withMsg.getResolvedObj()), "resolved obj");
        String ts = withMsg.toString();
        check(ts.contains("javax.naming.LimitExceededException") && ts.contains("reason"), "toString naming");
        check(withMsg instanceof NamingException, "catchable as NamingException");
        check(new javax.naming.SizeLimitExceededException("s") instanceof javax.naming.LimitExceededException, "SizeLimit is-a LimitExceeded");
        check(new javax.naming.TimeLimitExceededException("t") instanceof javax.naming.LimitExceededException, "TimeLimit is-a LimitExceeded");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
