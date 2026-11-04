/*
 * @test
 * @summary java.security.spec.PKCS8EncodedKeySpec conformance: white-box structure + black-box behavior.
 * @run main PKCS8EncodedKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.EncodedKeySpec;
import java.util.Arrays;

public class PKCS8EncodedKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = PKCS8EncodedKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.spec.EncodedKeySpec"), "superclass EncodedKeySpec");
        checkInterfaces(c, new String[]{"java.security.BinaryEncodable"});
        checkMethods(c, new String[]{"getEncoded","getFormat"});
    }

    static void testBlackBox() {
        byte[] key = new byte[]{1,2,3,4};
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
        check("PKCS#8".equals(spec.getFormat()), "getFormat");
        byte[] got = spec.getEncoded();
        check(got != key, "getEncoded defensive copy");
        check(Arrays.equals(key, got), "getEncoded content");
        got[0] = 99;
        check(spec.getEncoded()[0] == 1, "getEncoded fresh copy each call");
        key[0] = 88;
        check(spec.getEncoded()[0] == 1, "constructor defensive copy of input");

        check(spec.getAlgorithm() == null, "getAlgorithm null single-arg");
        PKCS8EncodedKeySpec spec2 = new PKCS8EncodedKeySpec(key, "RSA");
        check("RSA".equals(spec2.getAlgorithm()), "getAlgorithm 2-arg");
        check(spec instanceof EncodedKeySpec, "instanceof EncodedKeySpec");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
