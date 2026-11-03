/*
 * @test
 * @summary java.sql.JDBCType conformance: white-box structure + black-box behavior.
 * @run main JDBCTypeTest
 */
public class JDBCTypeTest {
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
        Class<?> c = Class.forName("java.sql.JDBCType");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.SQLType"), "superinterface 0");
        check(c.getDeclaredMethods().length == 7, "declared method count 7");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.sql.JDBCType[] all = java.sql.JDBCType.values();
        check(all.length == 41, "values() length 41");
        int[] vendors = {-7,-6,5,4,-5,6,7,8,2,3,1,12,-1,91,92,93,-2,-3,-4,0,1111,2000,2001,2002,2003,2004,2005,2006,70,16,-8,-15,-9,-16,2011,2009,2012,2013,2014,2015,2016};
        check(all.length == vendors.length, "one vendor number per constant");
        for (java.sql.JDBCType t : all) {
            check(t.getVendorTypeNumber() == vendors[t.ordinal()], "getVendorTypeNumber " + t.name());
            check(java.sql.JDBCType.valueOf(t.getVendorTypeNumber()) == t, "valueOf(int) roundtrip " + t.name());
            check(java.sql.JDBCType.valueOf(t.name()) == t, "valueOf(String) roundtrip " + t.name());
            check(t.getName().equals(t.name()), "getName " + t.name());
            check("java.sql".equals(t.getVendor()), "getVendor " + t.name());
        }
        check(java.sql.JDBCType.valueOf(4) == java.sql.JDBCType.INTEGER, "valueOf(4)==INTEGER");
        checkThrows(IllegalArgumentException.class, "valueOf invalid", () -> java.sql.JDBCType.valueOf(9999));
        java.sql.SQLType st = java.sql.JDBCType.INTEGER;
        check(st.getName().equals("INTEGER") && st.getVendorTypeNumber() == 4 && "java.sql".equals(st.getVendor()), "as SQLType");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
