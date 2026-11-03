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
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
}
