/*
 * @test
 * @summary java.security.spec.PSSParameterSpec conformance: white-box structure + black-box behavior.
 * @run main PSSParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.PSSParameterSpec;
import java.security.spec.MGF1ParameterSpec;

public class PSSParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = PSSParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getDigestAlgorithm","getMGFAlgorithm","getMGFParameters","getSaltLength","getTrailerField","toString"});
    }

    static void testBlackBox() {
        check(PSSParameterSpec.TRAILER_FIELD_BC == 1, "TRAILER_FIELD_BC == 1");
        check(PSSParameterSpec.DEFAULT != null, "DEFAULT non-null");
        check("SHA-1".equals(PSSParameterSpec.DEFAULT.getDigestAlgorithm()), "DEFAULT digest SHA-1");
        check("MGF1".equals(PSSParameterSpec.DEFAULT.getMGFAlgorithm()), "DEFAULT mgf MGF1");
        check(PSSParameterSpec.DEFAULT.getSaltLength() == 20, "DEFAULT salt 20");
        check(PSSParameterSpec.DEFAULT.getTrailerField() == 1, "DEFAULT trailer 1");

        PSSParameterSpec spec = new PSSParameterSpec(32);
        check(spec.getSaltLength() == 32, "int ctor getSaltLength");
        check("SHA-1".equals(spec.getDigestAlgorithm()), "int ctor getDigestAlgorithm SHA-1");
        check("MGF1".equals(spec.getMGFAlgorithm()), "int ctor getMGFAlgorithm MGF1");
        check(spec.getTrailerField() == PSSParameterSpec.TRAILER_FIELD_BC, "int ctor getTrailerField");

        MGF1ParameterSpec mgf = new MGF1ParameterSpec("SHA-512");
        PSSParameterSpec full = new PSSParameterSpec("SHA-512", "MGF1", mgf, 64, 1);
        check("SHA-512".equals(full.getDigestAlgorithm()), "full getDigestAlgorithm");
        check("MGF1".equals(full.getMGFAlgorithm()), "full getMGFAlgorithm");
        check(mgf == full.getMGFParameters(), "full getMGFParameters");
        check(full.getSaltLength() == 64, "full getSaltLength");
        check(full.getTrailerField() == 1, "full getTrailerField");
        check(full.toString() != null && full.toString().contains("PSSParameterSpec"), "toString");

        check(spec instanceof java.security.spec.AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
