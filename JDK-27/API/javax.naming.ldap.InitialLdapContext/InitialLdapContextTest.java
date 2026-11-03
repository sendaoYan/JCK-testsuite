/*
 * @test
 * @summary javax.naming.ldap.InitialLdapContext conformance: white-box structure (no network).
 * @run main InitialLdapContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;

public class InitialLdapContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.InitialLdapContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == InitialDirContext.class, "extends InitialDirContext");
        check(LdapContext.class.isAssignableFrom(c), "implements LdapContext");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 0), "no-arg ctor");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 2 && x.getParameterTypes()[0] == Hashtable.class && x.getParameterTypes()[1] == Control[].class), "ctor(Hashtable,Control[])");
        check(hasMethod(c, "extendedOperation"), "extendedOperation");
        check(hasMethod(c, "newInstance"), "newInstance");
        check(hasMethod(c, "reconnect"), "reconnect");
        check(hasMethod(c, "getConnectControls"), "getConnectControls");
        check(hasMethod(c, "setRequestControls"), "setRequestControls");
        check(hasMethod(c, "getRequestControls"), "getRequestControls");
        check(hasMethod(c, "getResponseControls"), "getResponseControls");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
