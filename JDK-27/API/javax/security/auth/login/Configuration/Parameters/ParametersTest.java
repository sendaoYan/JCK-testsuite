/*
 * @test
 * @summary javax.security.auth.login.Configuration.Parameters conformance: white-box structure + black-box behavior.
 * @run main ParametersTest
 */

import javax.security.auth.login.Configuration;

public class ParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.login.Configuration$Parameters");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getDeclaredMethods().length == 0, "marker interface declares no methods");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // implement the marker interface and use it as a parameter bag
        Configuration.Parameters p = new Configuration.Parameters() { };
        check(p instanceof Configuration.Parameters, "anonymous impl instanceof Parameters");

        // a user-defined implementation carrying state
        Configuration.Parameters withState = new Configuration.Parameters() {
            public String toString() { return "state=1"; }
        };
        check("state=1".equals(withState.toString()), "user impl carries its own state");

        // Parameters instances can be passed to Configuration.getInstance (unknown type
        // exercises parameter plumbing without any registered provider being required).
        boolean thrown = false;
        try {
            Configuration.getInstance("JckNoSuchConfigType", p);
        } catch (java.security.NoSuchAlgorithmException expected) {
            thrown = true;
        }
        check(thrown, "getInstance accepts a Parameters argument (throws NoSuchAlgorithmException)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
