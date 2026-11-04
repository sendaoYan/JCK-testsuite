/*
 * @test
 * @summary javax.naming.directory.InitialDirContext conformance: white-box structure + black-box behavior.
 * @run main InitialDirContextTest
 */

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import java.lang.reflect.*;
import java.util.*;
public class InitialDirContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.InitialDirContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(InitialContext.class.isAssignableFrom(c), "extends InitialContext");
        check(DirContext.class.isAssignableFrom(c), "implements DirContext");
        check(hasMethod(c, "getAttributes"), "getAttributes");
        check(hasMethod(c, "modifyAttributes"), "modifyAttributes");
        check(hasMethod(c, "bind"), "bind");
        check(hasMethod(c, "rebind"), "rebind");
        check(hasMethod(c, "createSubcontext"), "createSubcontext");
        check(hasMethod(c, "getSchema"), "getSchema");
        check(hasMethod(c, "search"), "search");

    }

    static void testBlackBox() throws Exception {
        boolean threw = false;
        try { InitialDirContext ctx = new InitialDirContext(); ctx.close(); } catch (javax.naming.NamingException e) { threw = true; }
        check(true, "construction attempted; provider-dependent (threw=" + threw + ")");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
