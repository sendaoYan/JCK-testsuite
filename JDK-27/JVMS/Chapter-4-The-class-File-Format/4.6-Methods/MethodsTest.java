/*
 * @test
 * @summary JVMS 4.6: method_info structures record a method's access flags and
 *          descriptor.  Reflection exposes the same flags and parameter/return
 *          types.
 * @run main MethodsTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodsTest {
    public static int pub(int a, String b) { return a + b.length(); }
    private void priv() { }
    protected void prot() { }
    void pack() { }
    static void stat() { }
    final void fin() { }
    synchronized void sync() { }
    static native void nat();

    public static void main(String[] args) throws Exception {
        Class<?> c = MethodsTest.class;

        Method pub = c.getMethod("pub", int.class, String.class);
        requireFlags(pub, Modifier.PUBLIC | Modifier.STATIC, "pub");
        if (pub.getReturnType() != int.class) throw new AssertionError("pub return type");
        if (pub.getParameterTypes().length != 2) throw new AssertionError("pub params");

        requireFlags(c.getDeclaredMethod("priv"), Modifier.PRIVATE, "priv");
        requireFlags(c.getDeclaredMethod("prot"), Modifier.PROTECTED, "prot");

        Method pack = c.getDeclaredMethod("pack");
        if ((pack.getModifiers() & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED)) != 0)
            throw new AssertionError("package method should have no access modifier");

        requireFlags(c.getDeclaredMethod("stat"), Modifier.STATIC, "stat");
        requireFlags(c.getDeclaredMethod("fin"), Modifier.FINAL, "fin");
        requireFlags(c.getDeclaredMethod("sync"), Modifier.SYNCHRONIZED, "sync");
        requireFlags(c.getDeclaredMethod("nat"), Modifier.NATIVE | Modifier.STATIC, "nat");

        System.out.println("PASS");
    }

    static void requireFlags(Method m, int mask, String name) {
        if ((m.getModifiers() & mask) != mask)
            throw new AssertionError(name + ": expected flags 0x" + Integer.toHexString(mask)
                + " but got 0x" + Integer.toHexString(m.getModifiers()));
    }
}
