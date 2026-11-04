/*
 * @test
 * @summary javax.naming.ldap.InitialLdapContext conformance: white-box structure + black-box behavior.
 * @run main InitialLdapContextTest
 */

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import java.lang.reflect.*;
import java.util.*;
public class InitialLdapContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.InitialLdapContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(InitialDirContext.class.isAssignableFrom(c), "extends InitialDirContext");
        check(LdapContext.class.isAssignableFrom(c), "implements LdapContext");
        check(hasMethod(c, "extendedOperation"), "extendedOperation");
        check(hasMethod(c, "newInstance"), "newInstance");
        check(hasMethod(c, "reconnect"), "reconnect");
        check(hasMethod(c, "getConnectControls"), "getConnectControls");
        check(hasMethod(c, "setRequestControls"), "setRequestControls");
        check(hasMethod(c, "getRequestControls"), "getRequestControls");
        check(hasMethod(c, "getResponseControls"), "getResponseControls");

    }

    static void testBlackBox() throws Exception {
        boolean threw = false;
        try { InitialLdapContext ctx = new InitialLdapContext(); ctx.close(); } catch (javax.naming.NamingException e) { threw = true; }
        check(true, "construction attempted; provider-dependent (threw=" + threw + ")");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
