/*
 * @test
 * @summary javax.naming.directory.DirContext conformance: white-box structure.
 * @run main DirContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

public class DirContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.DirContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(Context.class.isAssignableFrom(c), "extends Context");
        check(DirContext.ADD_ATTRIBUTE == 1, "ADD_ATTRIBUTE==1");
        check(DirContext.REPLACE_ATTRIBUTE == 2, "REPLACE_ATTRIBUTE==2");
        check(DirContext.REMOVE_ATTRIBUTE == 3, "REMOVE_ATTRIBUTE==3");
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
