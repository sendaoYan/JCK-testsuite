/*
 * @test
 * @summary javax.transaction.xa.Xid conformance: interface shape, supertypes and constants
 * @run main XidTest
 */

import java.lang.reflect.Modifier;

public class XidTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.transaction.xa.Xid");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 0, "0 direct superinterface(s)");
        check(c.getDeclaredMethods().length == 3, "declared method count 3");
        java.lang.reflect.Field f_MAXGTRIDSIZE = c.getField("MAXGTRIDSIZE");
        check(f_MAXGTRIDSIZE.getInt(null) == 64, "MAXGTRIDSIZE == 64");
        java.lang.reflect.Field f_MAXBQUALSIZE = c.getField("MAXBQUALSIZE");
        check(f_MAXBQUALSIZE.getInt(null) == 64, "MAXBQUALSIZE == 64");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
