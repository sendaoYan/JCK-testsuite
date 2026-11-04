/*
 * @test
 * @summary java.security.spec.DSAGenParameterSpec conformance: white-box structure + black-box behavior.
 * @run main DSAGenParameterSpecTest
 */

import java.lang.reflect.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAGenParameterSpec;

public class DSAGenParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = DSAGenParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getPrimePLength", "getSubprimeQLength", "getSeedLength"});
    }

    static void testBlackBox() {
        DSAGenParameterSpec spec = new DSAGenParameterSpec(2048, 256);
        check(spec.getPrimePLength() == 2048, "getPrimePLength");
        check(spec.getSubprimeQLength() == 256, "getSubprimeQLength");
        check(spec.getSeedLength() == 256, "getSeedLength default");
        DSAGenParameterSpec spec2 = new DSAGenParameterSpec(2048, 256, 512);
        check(spec2.getSeedLength() == 512, "getSeedLength");
        check(spec instanceof AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }

    // ---- 契约深化（appended, 第 2 批） ----
    static void testContract() {
        // ---- 契约深化：参数域矩阵（精确异常与消息） ----
        java.security.spec.DSAGenParameterSpec g1 = new java.security.spec.DSAGenParameterSpec(1024, 160);
        check(g1.getPrimePLength() == 1024 && g1.getSubprimeQLength() == 160 && g1.getSeedLength() == 160,
                "(1024,160): seedLen defaults to subprimeQLen");
        check(new java.security.spec.DSAGenParameterSpec(2048, 224).getSeedLength() == 224, "(2048,224): seedLen 224");
        check(new java.security.spec.DSAGenParameterSpec(2048, 256).getSeedLength() == 256, "(2048,256) valid");
        check(new java.security.spec.DSAGenParameterSpec(3072, 256).getPrimePLength() == 3072, "(3072,256) valid");
        check(new java.security.spec.DSAGenParameterSpec(2048, 256, 512).getSeedLength() == 512, "3-arg explicit seedLen 512");
        check(new java.security.spec.DSAGenParameterSpec(2048, 256, 256).getSeedLength() == 256, "3-arg seedLen == qLen boundary valid");
        checkThrowsMsg(java.lang.IllegalArgumentException.class, () -> new java.security.spec.DSAGenParameterSpec(0, 256), "primePLen", "P=0 rejected");
        checkThrowsMsg(java.lang.IllegalArgumentException.class, () -> new java.security.spec.DSAGenParameterSpec(-1, 256), "primePLen", "P<0 rejected");
        checkThrowsMsg(java.lang.IllegalArgumentException.class, () -> new java.security.spec.DSAGenParameterSpec(2048, 0), "subprimeQLen", "Q=0 rejected");
        checkThrowsMsg(java.lang.IllegalArgumentException.class, () -> new java.security.spec.DSAGenParameterSpec(2048, -1), "subprimeQLen", "Q<0 rejected");
        checkThrowsMsg(java.lang.IllegalArgumentException.class, () -> new java.security.spec.DSAGenParameterSpec(2048, 221), "224 or 256", "2048 allows only 224/256");
        checkThrowsMsg(java.lang.IllegalArgumentException.class, () -> new java.security.spec.DSAGenParameterSpec(2048, 160), "224 or 256", "2048 rejects 160");
        checkThrowsMsg(java.lang.IllegalArgumentException.class, () -> new java.security.spec.DSAGenParameterSpec(1024, 224), "160", "1024 allows only 160");
        checkThrowsMsg(java.lang.IllegalArgumentException.class, () -> new java.security.spec.DSAGenParameterSpec(3072, 160), "256", "3072 allows only 256");
        checkThrowsMsg(java.lang.IllegalArgumentException.class, () -> new java.security.spec.DSAGenParameterSpec(2048, 256, -1), "seedLen", "seedLen<0 rejected");
        checkThrowsMsg(java.lang.IllegalArgumentException.class, () -> new java.security.spec.DSAGenParameterSpec(2048, 256, 0), "seedLen", "seedLen 0 rejected");
        check(g1 instanceof java.security.spec.AlgorithmParameterSpec, "implements AlgorithmParameterSpec");
        check(java.security.spec.AlgorithmParameterSpec.class.isAssignableFrom(java.security.spec.DSAGenParameterSpec.class), "isAssignableFrom");
        check(java.lang.reflect.Modifier.isFinal(java.security.spec.DSAGenParameterSpec.class.getModifiers()), "final class");
        check(!g1.equals(new java.security.spec.DSAGenParameterSpec(1024, 160)), "no equals override: identity semantics");
        check(g1.equals(g1), "self-equality");
        check(g1.hashCode() == g1.hashCode(), "hashCode stable");
        check(!java.io.Serializable.class.isAssignableFrom(java.security.spec.DSAGenParameterSpec.class), "not Serializable");
        check(g1.toString().contains("DSAGenParameterSpec"), "default toString contains class name");
    }

    // ---- helpers for contract deepening (appended) ----
    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r, String sub, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                check(t.getMessage() != null && t.getMessage().contains(sub),
                        msg + ": message contains \"" + sub + "\" (actual: " + t.getMessage() + ")");
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static byte[] serializeToBytes(Object o) {
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            new java.io.ObjectOutputStream(bos).writeObject(o);
            return bos.toByteArray();
        } catch (java.io.IOException e) {
            throw new AssertionError("serialize failed: " + e, e);
        }
    }

    static Object deserializeFromBytes(byte[] b) {
        try {
            return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b)).readObject();
        } catch (Exception e) {
            throw new AssertionError("deserialize failed: " + e, e);
        }
    }
}
