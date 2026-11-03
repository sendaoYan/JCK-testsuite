/*
 * @test
 * @summary java.security.SecureRandomSpi conformance: white-box structure + black-box behavior.
 * @run main SecureRandomSpiTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.SecureRandomParameters;
import java.security.SecureRandomSpi;
import java.util.Arrays;

public class SecureRandomSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.SecureRandomSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkInterfaces(c, new String[]{"java.io.Serializable"});
        checkMethods(c, new String[]{"toString"});
    }

    static void testBlackBox() throws Exception {
        MyRandomSpi s = new MyRandomSpi();
        byte[] b = new byte[5];
        s.doSetSeed(new byte[]{1, 2});
        s.doNextBytes(b);
        check(Arrays.equals(b, new byte[]{7, 7, 7, 7, 7}), "engineNextBytes fills buffer");
        byte[] seed = s.doGenerateSeed(4);
        check(seed.length == 4 && Arrays.equals(seed, new byte[]{3, 3, 3, 3}), "engineGenerateSeed length/content");
        check(s.doGetParameters() == null, "default engineGetParameters == null");
        check(s.toString().equals("MyRandomSpi"), "toString == simple name");
        checkThrows(UnsupportedOperationException.class, () -> s.doNextBytes(b, null));
        checkThrows(UnsupportedOperationException.class, () -> s.doReseed(null));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static class MyRandomSpi extends SecureRandomSpi {
        protected void engineSetSeed(byte[] seed) { }
        protected void engineNextBytes(byte[] bytes) { Arrays.fill(bytes, (byte) 7); }
        protected byte[] engineGenerateSeed(int numBytes) { byte[] r = new byte[numBytes]; Arrays.fill(r, (byte) 3); return r; }
        void doSetSeed(byte[] s) { engineSetSeed(s); }
        void doNextBytes(byte[] b) { engineNextBytes(b); }
        byte[] doGenerateSeed(int n) { return engineGenerateSeed(n); }
        void doNextBytes(byte[] b, SecureRandomParameters p) { engineNextBytes(b, p); }
        void doReseed(SecureRandomParameters p) { engineReseed(p); }
        SecureRandomParameters doGetParameters() { return engineGetParameters(); }
    }

    static void checkThrows(Class<? extends Throwable> type, Runnable r) {
        try { r.run(); } catch (Throwable t) { check(type.isInstance(t), "throws " + type.getSimpleName()); return; }
        throw new AssertionError("expected " + type.getSimpleName());
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}
