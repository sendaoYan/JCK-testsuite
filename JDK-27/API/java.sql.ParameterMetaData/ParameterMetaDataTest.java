/*
 * @test
 * @summary java.sql.ParameterMetaData conformance: interface shape, supertypes and constants
 * @run main ParameterMetaDataTest
 */

import java.lang.reflect.Modifier;

public class ParameterMetaDataTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.ParameterMetaData");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.Wrapper"), "superinterface 0");
        check(c.getDeclaredMethods().length == 9, "declared method count 9");
        java.lang.reflect.Field f_parameterNoNulls = c.getField("parameterNoNulls");
        check(f_parameterNoNulls.getInt(null) == 0, "parameterNoNulls == 0");
        java.lang.reflect.Field f_parameterNullable = c.getField("parameterNullable");
        check(f_parameterNullable.getInt(null) == 1, "parameterNullable == 1");
        java.lang.reflect.Field f_parameterNullableUnknown = c.getField("parameterNullableUnknown");
        check(f_parameterNullableUnknown.getInt(null) == 2, "parameterNullableUnknown == 2");
        java.lang.reflect.Field f_parameterModeUnknown = c.getField("parameterModeUnknown");
        check(f_parameterModeUnknown.getInt(null) == 0, "parameterModeUnknown == 0");
        java.lang.reflect.Field f_parameterModeIn = c.getField("parameterModeIn");
        check(f_parameterModeIn.getInt(null) == 1, "parameterModeIn == 1");
        java.lang.reflect.Field f_parameterModeInOut = c.getField("parameterModeInOut");
        check(f_parameterModeInOut.getInt(null) == 2, "parameterModeInOut == 2");
        java.lang.reflect.Field f_parameterModeOut = c.getField("parameterModeOut");
        check(f_parameterModeOut.getInt(null) == 4, "parameterModeOut == 4");        System.out.println("PASS (" + passed + " assertions)");
    }
}
