/*
 * @test
 * @summary javax.sql.rowset.JoinRowSet conformance: interface shape, supertypes and constants
 * @run main JoinRowSetTest
 */

import java.lang.reflect.Modifier;

public class JoinRowSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.JoinRowSet");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.rowset.WebRowSet"), "superinterface 0");
        check(c.getDeclaredMethods().length == 16, "declared method count 16");
        java.lang.reflect.Field f_CROSS_JOIN = c.getField("CROSS_JOIN");
        check(f_CROSS_JOIN.getInt(null) == 0, "CROSS_JOIN == 0");
        java.lang.reflect.Field f_INNER_JOIN = c.getField("INNER_JOIN");
        check(f_INNER_JOIN.getInt(null) == 1, "INNER_JOIN == 1");
        java.lang.reflect.Field f_LEFT_OUTER_JOIN = c.getField("LEFT_OUTER_JOIN");
        check(f_LEFT_OUTER_JOIN.getInt(null) == 2, "LEFT_OUTER_JOIN == 2");
        java.lang.reflect.Field f_RIGHT_OUTER_JOIN = c.getField("RIGHT_OUTER_JOIN");
        check(f_RIGHT_OUTER_JOIN.getInt(null) == 3, "RIGHT_OUTER_JOIN == 3");
        java.lang.reflect.Field f_FULL_JOIN = c.getField("FULL_JOIN");
        check(f_FULL_JOIN.getInt(null) == 4, "FULL_JOIN == 4");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
