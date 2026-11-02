/*
 * @test
 * @summary java.sql.ResultSet conformance: interface shape, supertypes and constants
 * @run main ResultSetTest
 */

import java.lang.reflect.Modifier;

public class ResultSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.ResultSet");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 2, "2 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.Wrapper"), "superinterface 0");
        check(ifs[1].getName().equals("java.lang.AutoCloseable"), "superinterface 1");
        check(c.getDeclaredMethods().length == 193, "declared method count 193");
        java.lang.reflect.Field f_FETCH_FORWARD = c.getField("FETCH_FORWARD");
        check(f_FETCH_FORWARD.getInt(null) == 1000, "FETCH_FORWARD == 1000");
        java.lang.reflect.Field f_FETCH_REVERSE = c.getField("FETCH_REVERSE");
        check(f_FETCH_REVERSE.getInt(null) == 1001, "FETCH_REVERSE == 1001");
        java.lang.reflect.Field f_FETCH_UNKNOWN = c.getField("FETCH_UNKNOWN");
        check(f_FETCH_UNKNOWN.getInt(null) == 1002, "FETCH_UNKNOWN == 1002");
        java.lang.reflect.Field f_TYPE_FORWARD_ONLY = c.getField("TYPE_FORWARD_ONLY");
        check(f_TYPE_FORWARD_ONLY.getInt(null) == 1003, "TYPE_FORWARD_ONLY == 1003");
        java.lang.reflect.Field f_TYPE_SCROLL_INSENSITIVE = c.getField("TYPE_SCROLL_INSENSITIVE");
        check(f_TYPE_SCROLL_INSENSITIVE.getInt(null) == 1004, "TYPE_SCROLL_INSENSITIVE == 1004");
        java.lang.reflect.Field f_TYPE_SCROLL_SENSITIVE = c.getField("TYPE_SCROLL_SENSITIVE");
        check(f_TYPE_SCROLL_SENSITIVE.getInt(null) == 1005, "TYPE_SCROLL_SENSITIVE == 1005");
        java.lang.reflect.Field f_CONCUR_READ_ONLY = c.getField("CONCUR_READ_ONLY");
        check(f_CONCUR_READ_ONLY.getInt(null) == 1007, "CONCUR_READ_ONLY == 1007");
        java.lang.reflect.Field f_CONCUR_UPDATABLE = c.getField("CONCUR_UPDATABLE");
        check(f_CONCUR_UPDATABLE.getInt(null) == 1008, "CONCUR_UPDATABLE == 1008");
        java.lang.reflect.Field f_HOLD_CURSORS_OVER_COMMIT = c.getField("HOLD_CURSORS_OVER_COMMIT");
        check(f_HOLD_CURSORS_OVER_COMMIT.getInt(null) == 1, "HOLD_CURSORS_OVER_COMMIT == 1");
        java.lang.reflect.Field f_CLOSE_CURSORS_AT_COMMIT = c.getField("CLOSE_CURSORS_AT_COMMIT");
        check(f_CLOSE_CURSORS_AT_COMMIT.getInt(null) == 2, "CLOSE_CURSORS_AT_COMMIT == 2");        System.out.println("PASS (" + passed + " assertions)");
    }
}
