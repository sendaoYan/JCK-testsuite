/*
 * @test
 * @summary javax.security.sasl.SaslServerFactory conformance: white-box structure + black-box behavior.
 * @run main SaslServerFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.security.sasl.*;

public class SaslServerFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.sasl.SaslServerFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"createSaslServer","getMechanismNames"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Enumeration<SaslServerFactory> factories = Sasl.getSaslServerFactories();
        check(factories != null, "factories non-null");

        int count = 0;
        Set<String> allMechs = new TreeSet<>();
        SaslServerFactory digestFactory = null;
        while (factories.hasMoreElements()) {
            SaslServerFactory f = factories.nextElement();
            count++;
            String[] mechs = f.getMechanismNames(new HashMap<String, String>());
            check(mechs != null, "getMechanismNames non-null");
            for (String m : mechs) {
                allMechs.add(m);
                if ("DIGEST-MD5".equals(m) && digestFactory == null) digestFactory = f;
            }
            check(f.createSaslServer("NO-SUCH-MECH", null, null, null, null) == null,
                  "unknown mechanism returns null");
        }
        check(count > 0, "at least one factory");
        check(!allMechs.isEmpty(), "union of mechanisms non-empty");

        // DIGEST-MD5 requires a callback handler and throws when none is given.
        if (digestFactory != null) {
            try {
                digestFactory.createSaslServer("DIGEST-MD5", "DIGEST-MD5", "localhost", null, null);
                check(false, "DIGEST-MD5 without handler should throw");
            } catch (SaslException e) {
                check(true, "DIGEST-MD5 without handler throws SaslException");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
