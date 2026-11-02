/*
 * @test
 * @summary javax.sql.rowset.CachedRowSet conformance: interface shape, supertypes and constants
 * @run main CachedRowSetTest
 */

import java.lang.reflect.Modifier;

public class CachedRowSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.CachedRowSet");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 2, "2 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.RowSet"), "superinterface 0");
        check(ifs[1].getName().equals("javax.sql.rowset.Joinable"), "superinterface 1");
        check(c.getDeclaredMethods().length == 41, "declared method count 41");
        java.lang.reflect.Field f_COMMIT_ON_ACCEPT_CHANGES = c.getField("COMMIT_ON_ACCEPT_CHANGES");
        check(f_COMMIT_ON_ACCEPT_CHANGES.getBoolean(null) == true, "COMMIT_ON_ACCEPT_CHANGES == true");        System.out.println("PASS (" + passed + " assertions)");
    }
}
