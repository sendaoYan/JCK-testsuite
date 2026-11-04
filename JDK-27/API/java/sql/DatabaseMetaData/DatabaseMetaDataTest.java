/*
 * @test
 * @summary java.sql.DatabaseMetaData conformance: white-box structure + black-box behavior.
 * @run main DatabaseMetaDataTest
 */
public class DatabaseMetaDataTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface Throwing { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> t, String label, Throwing r) {
        try { r.run(); } catch (Throwable x) { check(t.isInstance(x), label + " throws " + t.getSimpleName()); return; }
        throw new AssertionError(label + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.sql.DatabaseMetaData");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.Wrapper"), "superinterface 0");
        check(c.getDeclaredMethods().length == 177, "declared method count 177");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
java.sql.DatabaseMetaData dm = (java.sql.DatabaseMetaData) java.lang.reflect.Proxy.newProxyInstance(DatabaseMetaDataTest.class.getClassLoader(), new Class[]{java.sql.DatabaseMetaData.class}, (p, m, a) -> java.lang.reflect.InvocationHandler.invokeDefault(p, m, a));
check(dm.getMaxLogicalLobSize() == 0, "getMaxLogicalLobSize default 0");
check(!dm.supportsRefCursors(), "supportsRefCursors default false");
check(!dm.supportsSharding(), "supportsSharding default false");
check(java.sql.DatabaseMetaData.typeNoNulls == 0, "typeNoNulls");
check(java.sql.DatabaseMetaData.typeNullable == 1, "typeNullable");
check(java.sql.DatabaseMetaData.typeNullableUnknown == 2, "typeNullableUnknown");
check(java.sql.DatabaseMetaData.sqlStateSQL == 2, "sqlStateSQL");
check(java.sql.DatabaseMetaData.importedKeyCascade == 0, "importedKeyCascade");
check(java.sql.DatabaseMetaData.importedKeyRestrict == 1, "importedKeyRestrict");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
