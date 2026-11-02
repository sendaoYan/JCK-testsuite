/*
 * @test
 * @summary javax.sql.rowset.spi.SyncResolver conformance: interface shape, supertypes and constants
 * @run main SyncResolverTest
 */

import java.lang.reflect.Modifier;

public class SyncResolverTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.spi.SyncResolver");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.RowSet"), "superinterface 0");
        check(c.getDeclaredMethods().length == 7, "declared method count 7");
        java.lang.reflect.Field f_UPDATE_ROW_CONFLICT = c.getField("UPDATE_ROW_CONFLICT");
        check(f_UPDATE_ROW_CONFLICT.getInt(null) == 0, "UPDATE_ROW_CONFLICT == 0");
        java.lang.reflect.Field f_DELETE_ROW_CONFLICT = c.getField("DELETE_ROW_CONFLICT");
        check(f_DELETE_ROW_CONFLICT.getInt(null) == 1, "DELETE_ROW_CONFLICT == 1");
        java.lang.reflect.Field f_INSERT_ROW_CONFLICT = c.getField("INSERT_ROW_CONFLICT");
        check(f_INSERT_ROW_CONFLICT.getInt(null) == 2, "INSERT_ROW_CONFLICT == 2");
        java.lang.reflect.Field f_NO_ROW_CONFLICT = c.getField("NO_ROW_CONFLICT");
        check(f_NO_ROW_CONFLICT.getInt(null) == 3, "NO_ROW_CONFLICT == 3");        System.out.println("PASS (" + passed + " assertions)");
    }
}
