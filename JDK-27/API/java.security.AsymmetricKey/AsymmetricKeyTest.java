/*
 * @test
 * @summary java.security.AsymmetricKey conformance: white-box structure + black-box behavior.
 * @run main AsymmetricKeyTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AsymmetricKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class AsymmetricKeyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.AsymmetricKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.Key", "java.security.BinaryEncodable"});
        checkMethods(c, new String[]{"getParams"});
    }

    static void testBlackBox() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        KeyPair kp = g.generateKeyPair();
        PublicKey pub = kp.getPublic();
        PrivateKey priv = kp.getPrivate();
        check(pub instanceof AsymmetricKey, "RSA public key is AsymmetricKey");
        check(priv instanceof AsymmetricKey, "RSA private key is AsymmetricKey");
        check(((AsymmetricKey) pub).getParams() == null, "RSA public getParams == null");
        check(((AsymmetricKey) priv).getParams() == null, "RSA private getParams == null");
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
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}
