/*
 * @test
 * @summary java.sql.ParameterMetaData conformance: white-box structure + black-box behavior.
 * @run main ParameterMetaDataTest
 */
public class ParameterMetaDataTest {
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
        Class<?> c = Class.forName("java.sql.ParameterMetaData");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.Wrapper"), "superinterface 0");
        check(c.getDeclaredMethods().length == 9, "declared method count 9");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
check(java.sql.ParameterMetaData.parameterNoNulls == 0, "parameterNoNulls");
check(java.sql.ParameterMetaData.parameterNullable == 1, "parameterNullable");
check(java.sql.ParameterMetaData.parameterNullableUnknown == 2, "parameterNullableUnknown");
check(java.sql.ParameterMetaData.parameterModeUnknown == 0, "parameterModeUnknown");
check(java.sql.ParameterMetaData.parameterModeIn == 1, "parameterModeIn");
check(java.sql.ParameterMetaData.parameterModeInOut == 2, "parameterModeInOut");
check(java.sql.ParameterMetaData.parameterModeOut == 4, "parameterModeOut");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
