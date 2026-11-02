/*
 * @test
 * @summary javax.sql.RowSetMetaData conformance: interface shape, supertypes and constants
 * @run main RowSetMetaDataTest
 */

import java.lang.reflect.Modifier;

public class RowSetMetaDataTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.RowSetMetaData");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.ResultSetMetaData"), "superinterface 0");
        check(c.getDeclaredMethods().length == 17, "declared method count 17");        System.out.println("PASS (" + passed + " assertions)");
    }
}
