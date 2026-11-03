/*
 * @test
 * @summary java.security.spec.EncodedKeySpec conformance: white-box structure + black-box behavior.
 * @run main EncodedKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

public class EncodedKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = EncodedKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getAlgorithm","getEncoded","getFormat"});
        boolean abs = false;
        try { Method m = c.getDeclaredMethod("getFormat"); abs = Modifier.isAbstract(m.getModifiers()); } catch (NoSuchMethodException e) { }
        check(abs, "getFormat abstract");
    }

    static void testBlackBox() {
        byte[] key = new byte[]{1,2,3,4};
        X509EncodedKeySpec x = new X509EncodedKeySpec(key, "RSA");
        check(x.getAlgorithm() != null && !x.getAlgorithm().isEmpty(), "getAlgorithm non-null");
        check("RSA".equals(x.getAlgorithm()), "getAlgorithm passed");
        byte[] e = x.getEncoded();
        check(e != key, "getEncoded defensive copy");
        check(Arrays.equals(key, e), "getEncoded content");
        e[0] = 99;
        check(x.getEncoded()[0] == 1, "getEncoded fresh copy each call");

        PKCS8EncodedKeySpec p = new PKCS8EncodedKeySpec(key);
        check(p.getAlgorithm() == null, "getAlgorithm null single-arg");
        check(EncodedKeySpec.class.isInstance(x), "subclass instanceof EncodedKeySpec");
        check(EncodedKeySpec.class.isInstance(p), "PKCS8 subclass instanceof EncodedKeySpec");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
