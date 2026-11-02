/*
 * @test
 * @summary java.sql.ResultSetMetaData conformance: interface shape, supertypes and constants
 * @run main ResultSetMetaDataTest
 */

import java.lang.reflect.Modifier;

public class ResultSetMetaDataTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.ResultSetMetaData");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.Wrapper"), "superinterface 0");
        check(c.getDeclaredMethods().length == 21, "declared method count 21");
        java.lang.reflect.Field f_columnNoNulls = c.getField("columnNoNulls");
        check(f_columnNoNulls.getInt(null) == 0, "columnNoNulls == 0");
        java.lang.reflect.Field f_columnNullable = c.getField("columnNullable");
        check(f_columnNullable.getInt(null) == 1, "columnNullable == 1");
        java.lang.reflect.Field f_columnNullableUnknown = c.getField("columnNullableUnknown");
        check(f_columnNullableUnknown.getInt(null) == 2, "columnNullableUnknown == 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
