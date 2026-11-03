/*
 * @test
 * @summary javax.naming.spi.NamingManager conformance: white-box structure + black-box behavior.
 * @run main NamingManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NoInitialContextException;
import javax.naming.spi.NamingManager;

public class NamingManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.NamingManager");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "getObjectInstance"), "getObjectInstance");
        check(hasMethod(c, "getStateToBind"), "getStateToBind");
        check(hasMethod(c, "getInitialContext"), "getInitialContext");
        check(hasMethod(c, "hasInitialContextFactoryBuilder"), "hasInitialContextFactoryBuilder");
        check(hasMethod(c, "setInitialContextFactoryBuilder"), "setInitialContextFactoryBuilder");
        check(hasMethod(c, "setObjectFactoryBuilder"), "setObjectFactoryBuilder");
        check(hasMethod(c, "getContinuationContext"), "getContinuationContext");
        check(hasMethod(c, "getURLContext"), "getURLContext");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Documented CPE constant.
        check("java.naming.spi.CannotProceedException".equals(NamingManager.CPE), "CPE constant value");

        // No builder installed by default in a fresh JVM.
        check(!NamingManager.hasInitialContextFactoryBuilder(), "no initial context factory builder");

        // getObjectInstance: ordinary object is returned unchanged when no factory applies.
        String plain = "plain-object";
        check(NamingManager.getObjectInstance(plain, null, null, null) == plain,
                "getObjectInstance returns plain object unchanged");
        check(NamingManager.getObjectInstance(null, null, null, null) == null,
                "getObjectInstance(null) returns null");

        // getStateToBind: ordinary object is returned unchanged when no state factory applies.
        check(NamingManager.getStateToBind(plain, null, null, null) == plain,
                "getStateToBind returns plain object unchanged");

        // getInitialContext with no configured factory throws NoInitialContextException.
        boolean threw = false;
        try {
            NamingManager.getInitialContext(new Hashtable<>());
        } catch (NoInitialContextException expected) {
            threw = true;
        }
        check(threw, "getInitialContext(empty) throws NoInitialContextException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
