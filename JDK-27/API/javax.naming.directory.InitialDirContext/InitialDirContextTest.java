/*
 * @test
 * @summary javax.naming.directory.InitialDirContext conformance: white-box structure (no network).
 * @run main InitialDirContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

public class InitialDirContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.InitialDirContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == InitialContext.class, "extends InitialContext");
        check(DirContext.class.isAssignableFrom(c), "implements DirContext");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 0), "no-arg ctor");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == Hashtable.class), "ctor(Hashtable)");
        check(hasMethod(c, "getAttributes"), "getAttributes");
        check(hasMethod(c, "modifyAttributes"), "modifyAttributes");
        check(hasMethod(c, "bind"), "bind");
        check(hasMethod(c, "rebind"), "rebind");
        check(hasMethod(c, "createSubcontext"), "createSubcontext");
        check(hasMethod(c, "getSchema"), "getSchema");
        check(hasMethod(c, "getSchemaClassDefinition"), "getSchemaClassDefinition");
        check(hasMethod(c, "search"), "search");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
