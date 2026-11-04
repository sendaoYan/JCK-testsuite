/*
 * @test
 * @summary java.sql.SQLType conformance: white-box structure + black-box behavior.
 * @run main SQLTypeTest
 */
public class SQLTypeTest {
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
        Class<?> c = Class.forName("java.sql.SQLType");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 3, "declared method count 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.sql.SQLType st = java.sql.JDBCType.VARCHAR;
        check("VARCHAR".equals(st.getName()), "getName");
        check("java.sql".equals(st.getVendor()), "getVendor");
        check(st.getVendorTypeNumber() == 12, "getVendorTypeNumber");
        java.sql.SQLType custom = new java.sql.SQLType() {
            public String getName() { return "CUSTOM"; }
            public String getVendor() { return "vendor"; }
            public Integer getVendorTypeNumber() { return 1234; }
        };
        check("CUSTOM".equals(custom.getName()), "custom getName");
        check("vendor".equals(custom.getVendor()), "custom getVendor");
        check(custom.getVendorTypeNumber() == 1234, "custom getVendorTypeNumber");
        check(custom.getVendorTypeNumber() != st.getVendorTypeNumber(), "distinct vendor numbers");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
