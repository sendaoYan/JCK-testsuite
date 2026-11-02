/*
 * @test
 * @summary javax.sql.rowset.JdbcRowSet conformance: interface shape, supertypes and constants
 * @run main JdbcRowSetTest
 */

import java.lang.reflect.Modifier;

public class JdbcRowSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.JdbcRowSet");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 2, "2 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.RowSet"), "superinterface 0");
        check(ifs[1].getName().equals("javax.sql.rowset.Joinable"), "superinterface 1");
        check(c.getDeclaredMethods().length == 8, "declared method count 8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
