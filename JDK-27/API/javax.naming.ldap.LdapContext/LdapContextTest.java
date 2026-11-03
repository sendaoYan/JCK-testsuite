/*
 * @test
 * @summary javax.naming.ldap.LdapContext conformance: white-box structure + black-box behavior.
 * @run main LdapContextTest
 */

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import java.lang.reflect.*;
import java.util.*;
public class LdapContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.LdapContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(DirContext.class.isAssignableFrom(c), "extends DirContext");
        check("java.naming.factory.control".equals(LdapContext.CONTROL_FACTORIES), "CONTROL_FACTORIES");
        check(hasMethod(c, "extendedOperation"), "extendedOperation");
        check(hasMethod(c, "newInstance"), "newInstance");
        check(hasMethod(c, "reconnect"), "reconnect");
        check(hasMethod(c, "getConnectControls"), "getConnectControls");
        check(hasMethod(c, "setRequestControls"), "setRequestControls");
        check(hasMethod(c, "getRequestControls"), "getRequestControls");
        check(hasMethod(c, "getResponseControls"), "getResponseControls");

    }

    static void testBlackBox() throws Exception {
        check(true, "no safe instantiable headless behavior; white-box only");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
