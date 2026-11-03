/*
 * @test
 * @summary java.sql.Struct conformance: white-box structure + black-box behavior.
 * @run main StructTest
 */
public class StructTest {
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
        Class<?> c = Class.forName("java.sql.Struct");
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
        java.sql.Struct s = new java.sql.Struct() {
            public String getSQLTypeName() { return "T"; }
            public Object[] getAttributes() { return new Object[]{"x", 1}; }
            public Object[] getAttributes(java.util.Map<String, Class<?>> m) { return new Object[]{"x"}; }
        };
        check("T".equals(s.getSQLTypeName()), "getSQLTypeName");
        check(s.getAttributes().length == 2, "getAttributes");
        check(s.getAttributes(new java.util.HashMap<String, Class<?>>()).length == 1, "getAttributes(map)");
        check(java.sql.Struct.class.isAssignableFrom(javax.sql.rowset.serial.SerialStruct.class), "SerialStruct implements Struct");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
