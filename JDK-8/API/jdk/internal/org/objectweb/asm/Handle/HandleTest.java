/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.Handle conformance: exception/value behavior.
 * @run main HandleTest
 */

import java.lang.reflect.Method;

public class HandleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.Handle", false, HandleTest.class.getClassLoader());
        Object h = c.getConstructor(int.class, String.class, String.class, String.class).newInstance(1, "owner", "name", "desc");
        check(((Integer) c.getMethod("getTag").invoke(h)).intValue() == 1, "getTag");
        check("owner".equals(c.getMethod("getOwner").invoke(h)), "getOwner");
        check("name".equals(c.getMethod("getName").invoke(h)), "getName");
        check("desc".equals(c.getMethod("getDesc").invoke(h)), "getDesc");
        Object h2 = c.getConstructor(int.class, String.class, String.class, String.class).newInstance(1, "owner", "name", "desc");
        check(((Boolean) c.getMethod("equals", Object.class).invoke(h, h2)).booleanValue(), "equals");
        check(c.getMethod("hashCode").invoke(h).equals(c.getMethod("hashCode").invoke(h2)), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
