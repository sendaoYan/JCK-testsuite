/*
 * @test
 * @summary java.sql.Statement conformance: interface shape, supertypes and constants
 * @run main StatementTest
 */

import java.lang.reflect.Modifier;

public class StatementTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.Statement");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 2, "2 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.Wrapper"), "superinterface 0");
        check(ifs[1].getName().equals("java.lang.AutoCloseable"), "superinterface 1");
        check(c.getDeclaredMethods().length == 54, "declared method count 54");
        java.lang.reflect.Field f_CLOSE_CURRENT_RESULT = c.getField("CLOSE_CURRENT_RESULT");
        check(f_CLOSE_CURRENT_RESULT.getInt(null) == 1, "CLOSE_CURRENT_RESULT == 1");
        java.lang.reflect.Field f_KEEP_CURRENT_RESULT = c.getField("KEEP_CURRENT_RESULT");
        check(f_KEEP_CURRENT_RESULT.getInt(null) == 2, "KEEP_CURRENT_RESULT == 2");
        java.lang.reflect.Field f_CLOSE_ALL_RESULTS = c.getField("CLOSE_ALL_RESULTS");
        check(f_CLOSE_ALL_RESULTS.getInt(null) == 3, "CLOSE_ALL_RESULTS == 3");
        java.lang.reflect.Field f_SUCCESS_NO_INFO = c.getField("SUCCESS_NO_INFO");
        check(f_SUCCESS_NO_INFO.getInt(null) == -2, "SUCCESS_NO_INFO == -2");
        java.lang.reflect.Field f_EXECUTE_FAILED = c.getField("EXECUTE_FAILED");
        check(f_EXECUTE_FAILED.getInt(null) == -3, "EXECUTE_FAILED == -3");
        java.lang.reflect.Field f_RETURN_GENERATED_KEYS = c.getField("RETURN_GENERATED_KEYS");
        check(f_RETURN_GENERATED_KEYS.getInt(null) == 1, "RETURN_GENERATED_KEYS == 1");
        java.lang.reflect.Field f_NO_GENERATED_KEYS = c.getField("NO_GENERATED_KEYS");
        check(f_NO_GENERATED_KEYS.getInt(null) == 2, "NO_GENERATED_KEYS == 2");        System.out.println("PASS (" + passed + " assertions)");
    }
}
