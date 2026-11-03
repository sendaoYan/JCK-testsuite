/*
 * @test
 * @summary java.sql.Ref conformance: white-box structure + black-box behavior.
 * @run main RefTest
 */
public class RefTest {
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
        Class<?> c = Class.forName("java.sql.Ref");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 4, "declared method count 4");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.sql.Ref r = new java.sql.Ref() {
            public String getBaseTypeName() { return "T"; }
            public Object getObject() { return "v"; }
            public Object getObject(java.util.Map<String, Class<?>> m) { return "v"; }
            public void setObject(Object o) {}
        };
        check("T".equals(r.getBaseTypeName()), "getBaseTypeName");
        check("v".equals(r.getObject()), "getObject");
        check(java.sql.Ref.class.isAssignableFrom(javax.sql.rowset.serial.SerialRef.class), "SerialRef implements Ref");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
