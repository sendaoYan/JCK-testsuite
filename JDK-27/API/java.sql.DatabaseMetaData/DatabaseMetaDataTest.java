/*
 * @test
 * @summary java.sql.DatabaseMetaData conformance: interface shape, supertypes and constants
 * @run main DatabaseMetaDataTest
 */

import java.lang.reflect.Modifier;

public class DatabaseMetaDataTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.DatabaseMetaData");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.Wrapper"), "superinterface 0");
        check(c.getDeclaredMethods().length == 177, "declared method count 177");
        java.lang.reflect.Field f_procedureResultUnknown = c.getField("procedureResultUnknown");
        check(f_procedureResultUnknown.getInt(null) == 0, "procedureResultUnknown == 0");
        java.lang.reflect.Field f_procedureNoResult = c.getField("procedureNoResult");
        check(f_procedureNoResult.getInt(null) == 1, "procedureNoResult == 1");
        java.lang.reflect.Field f_procedureReturnsResult = c.getField("procedureReturnsResult");
        check(f_procedureReturnsResult.getInt(null) == 2, "procedureReturnsResult == 2");
        java.lang.reflect.Field f_importedKeyCascade = c.getField("importedKeyCascade");
        check(f_importedKeyCascade.getInt(null) == 0, "importedKeyCascade == 0");
        java.lang.reflect.Field f_importedKeyRestrict = c.getField("importedKeyRestrict");
        check(f_importedKeyRestrict.getInt(null) == 1, "importedKeyRestrict == 1");
        java.lang.reflect.Field f_importedKeySetNull = c.getField("importedKeySetNull");
        check(f_importedKeySetNull.getInt(null) == 2, "importedKeySetNull == 2");
        java.lang.reflect.Field f_typeNoNulls = c.getField("typeNoNulls");
        check(f_typeNoNulls.getInt(null) == 0, "typeNoNulls == 0");
        java.lang.reflect.Field f_typeNullable = c.getField("typeNullable");
        check(f_typeNullable.getInt(null) == 1, "typeNullable == 1");
        java.lang.reflect.Field f_typeNullableUnknown = c.getField("typeNullableUnknown");
        check(f_typeNullableUnknown.getInt(null) == 2, "typeNullableUnknown == 2");
        java.lang.reflect.Field f_typeSearchable = c.getField("typeSearchable");
        check(f_typeSearchable.getInt(null) == 3, "typeSearchable == 3");
        java.lang.reflect.Field f_tableIndexStatistic = c.getField("tableIndexStatistic");
        check(f_tableIndexStatistic.getInt(null) == 0, "tableIndexStatistic == 0");
        java.lang.reflect.Field f_sqlStateXOpen = c.getField("sqlStateXOpen");
        check(f_sqlStateXOpen.getInt(null) == 1, "sqlStateXOpen == 1");
        java.lang.reflect.Field f_sqlStateSQL = c.getField("sqlStateSQL");
        check(f_sqlStateSQL.getInt(null) == 2, "sqlStateSQL == 2");
        java.lang.reflect.Field f_functionNoTable = c.getField("functionNoTable");
        check(f_functionNoTable.getInt(null) == 1, "functionNoTable == 1");
        java.lang.reflect.Field f_functionReturnsTable = c.getField("functionReturnsTable");
        check(f_functionReturnsTable.getInt(null) == 2, "functionReturnsTable == 2");
        java.lang.reflect.Field f_bestRowNotPseudo = c.getField("bestRowNotPseudo");
        check(f_bestRowNotPseudo.getInt(null) == 1, "bestRowNotPseudo == 1");
        java.lang.reflect.Field f_bestRowPseudo = c.getField("bestRowPseudo");
        check(f_bestRowPseudo.getInt(null) == 2, "bestRowPseudo == 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
