/*
 * @test
 * @summary javax.security.sasl.SaslClientFactory conformance: white-box structure + black-box behavior.
 * @run main SaslClientFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.security.sasl.*;
import javax.security.auth.callback.*;

public class SaslClientFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.sasl.SaslClientFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"createSaslClient","getMechanismNames"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CallbackHandler h = callbacks -> {
            for (Callback cb : callbacks) {
                if (cb instanceof NameCallback) ((NameCallback) cb).setName("user");
                else if (cb instanceof PasswordCallback) ((PasswordCallback) cb).setPassword("pass".toCharArray());
            }
        };

        Enumeration<SaslClientFactory> factories = Sasl.getSaslClientFactories();
        check(factories != null, "factories non-null");

        int count = 0;
        boolean sawPlain = false;
        SaslClientFactory plainFactory = null;
        while (factories.hasMoreElements()) {
            SaslClientFactory f = factories.nextElement();
            count++;
            String[] mechs = f.getMechanismNames(new HashMap<String, String>());
            check(mechs != null, "getMechanismNames non-null");
            for (String m : mechs) {
                if ("PLAIN".equals(m)) { sawPlain = true; if (plainFactory == null) plainFactory = f; }
            }
            check(f.createSaslClient(new String[]{"NO-SUCH-MECH"}, null, null, null, null, null) == null,
                  "unknown mechanism returns null");
        }
        check(count > 0, "at least one factory");
        check(sawPlain, "some factory advertises PLAIN");

        check(plainFactory != null, "found PLAIN factory");
        SaslClient sc = plainFactory.createSaslClient(new String[]{"PLAIN"}, null, "PLAIN", "localhost", null, h);
        check(sc != null, "createSaslClient PLAIN non-null");
        check("PLAIN".equals(sc.getMechanismName()), "created client mechanism");
        sc.dispose();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
