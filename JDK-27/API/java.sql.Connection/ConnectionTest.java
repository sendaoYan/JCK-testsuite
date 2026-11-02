/*
 * @test
 * @summary java.sql.Connection conformance: interface shape, supertypes and constants
 * @run main ConnectionTest
 */

import java.lang.reflect.Modifier;

public class ConnectionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.Connection");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 2, "2 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.Wrapper"), "superinterface 0");
        check(ifs[1].getName().equals("java.lang.AutoCloseable"), "superinterface 1");
        check(c.getDeclaredMethods().length == 62, "declared method count 62");
        java.lang.reflect.Field f_TRANSACTION_NONE = c.getField("TRANSACTION_NONE");
        check(f_TRANSACTION_NONE.getInt(null) == 0, "TRANSACTION_NONE == 0");
        java.lang.reflect.Field f_TRANSACTION_READ_UNCOMMITTED = c.getField("TRANSACTION_READ_UNCOMMITTED");
        check(f_TRANSACTION_READ_UNCOMMITTED.getInt(null) == 1, "TRANSACTION_READ_UNCOMMITTED == 1");
        java.lang.reflect.Field f_TRANSACTION_READ_COMMITTED = c.getField("TRANSACTION_READ_COMMITTED");
        check(f_TRANSACTION_READ_COMMITTED.getInt(null) == 2, "TRANSACTION_READ_COMMITTED == 2");
        java.lang.reflect.Field f_TRANSACTION_REPEATABLE_READ = c.getField("TRANSACTION_REPEATABLE_READ");
        check(f_TRANSACTION_REPEATABLE_READ.getInt(null) == 4, "TRANSACTION_REPEATABLE_READ == 4");
        java.lang.reflect.Field f_TRANSACTION_SERIALIZABLE = c.getField("TRANSACTION_SERIALIZABLE");
        check(f_TRANSACTION_SERIALIZABLE.getInt(null) == 8, "TRANSACTION_SERIALIZABLE == 8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
