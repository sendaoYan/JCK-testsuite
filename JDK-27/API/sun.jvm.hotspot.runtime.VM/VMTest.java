/*
 * @test
 * @summary sun.jvm.hotspot.runtime.VM conformance: internal class reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main VMTest
 */
public class VMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.VM", false, VMTest.class.getClassLoader());
        check(c != null, "loads");
        check(!c.isInterface(), "concrete");
        boolean f = false;
        for (java.lang.reflect.Field x : c.getDeclaredFields()) if (x.getName().equals("Flags_DEFAULT")) f = true;
        check(f, "Flags_DEFAULT field");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
