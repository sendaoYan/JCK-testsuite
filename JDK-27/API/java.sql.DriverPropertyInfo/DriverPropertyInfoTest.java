/*
 * @test
 * @summary java.sql.DriverPropertyInfo conformance: white-box structure + black-box behavior.
 * @run main DriverPropertyInfoTest
 */
public class DriverPropertyInfoTest {
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
        Class<?> c = Class.forName("java.sql.DriverPropertyInfo");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 0, "declared method count 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.sql.DriverPropertyInfo p = new java.sql.DriverPropertyInfo("user", "scott");
        check("user".equals(p.name), "name");
        check("scott".equals(p.value), "value from constructor");
        check(p.description == null, "description default null");
        check(!p.required, "required default false");
        check(p.choices == null, "choices default null");
        p.required = true;
        p.value = "tiger";
        p.description = "the user name";
        p.choices = new String[]{"a", "b"};
        check(p.required, "required set");
        check("tiger".equals(p.value), "value set");
        check("the user name".equals(p.description), "description set");
        check(p.choices.length == 2, "choices set");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
