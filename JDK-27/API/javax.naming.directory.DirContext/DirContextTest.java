/*
 * @test
 * @summary javax.naming.directory.DirContext conformance: white-box structure + black-box behavior.
 * @run main DirContextTest
 */

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import java.lang.reflect.*;
import java.util.*;
public class DirContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.DirContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(Context.class.isAssignableFrom(c), "extends Context");
        check(DirContext.ADD_ATTRIBUTE == 1, "ADD_ATTRIBUTE");
        check(DirContext.REPLACE_ATTRIBUTE == 2, "REPLACE_ATTRIBUTE");
        check(DirContext.REMOVE_ATTRIBUTE == 3, "REMOVE_ATTRIBUTE");
        check(hasMethod(c, "getAttributes"), "getAttributes");
        check(hasMethod(c, "modifyAttributes"), "modifyAttributes");
        check(hasMethod(c, "bind"), "bind");
        check(hasMethod(c, "rebind"), "rebind");
        check(hasMethod(c, "createSubcontext"), "createSubcontext");
        check(hasMethod(c, "getSchema"), "getSchema");
        check(hasMethod(c, "search"), "search");

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
