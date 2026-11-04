/*
 * @test
 * @summary java.security.spec.MGF1ParameterSpec conformance: white-box structure + black-box behavior.
 * @run main MGF1ParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.MGF1ParameterSpec;

public class MGF1ParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = MGF1ParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getDigestAlgorithm","toString"});
    }

    static void testBlackBox() {
        MGF1ParameterSpec spec = new MGF1ParameterSpec("SHA-256");
        check("SHA-256".equals(spec.getDigestAlgorithm()), "constructor getDigestAlgorithm");
        check(spec.toString().contains("SHA-256"), "toString contains digest");
        check(spec.toString().contains("MGF1ParameterSpec"), "toString contains class name");

        check("SHA-1".equals(MGF1ParameterSpec.SHA1.getDigestAlgorithm()), "SHA1");
        check("SHA-224".equals(MGF1ParameterSpec.SHA224.getDigestAlgorithm()), "SHA224");
        check("SHA-256".equals(MGF1ParameterSpec.SHA256.getDigestAlgorithm()), "SHA256");
        check("SHA-384".equals(MGF1ParameterSpec.SHA384.getDigestAlgorithm()), "SHA384");
        check("SHA-512".equals(MGF1ParameterSpec.SHA512.getDigestAlgorithm()), "SHA512");
        check("SHA-512/224".equals(MGF1ParameterSpec.SHA512_224.getDigestAlgorithm()), "SHA512_224");
        check("SHA-512/256".equals(MGF1ParameterSpec.SHA512_256.getDigestAlgorithm()), "SHA512_256");
        check("SHA3-224".equals(MGF1ParameterSpec.SHA3_224.getDigestAlgorithm()), "SHA3_224");
        check("SHA3-256".equals(MGF1ParameterSpec.SHA3_256.getDigestAlgorithm()), "SHA3_256");
        check("SHA3-384".equals(MGF1ParameterSpec.SHA3_384.getDigestAlgorithm()), "SHA3_384");
        check("SHA3-512".equals(MGF1ParameterSpec.SHA3_512.getDigestAlgorithm()), "SHA3_512");

        check(spec instanceof java.security.spec.AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
