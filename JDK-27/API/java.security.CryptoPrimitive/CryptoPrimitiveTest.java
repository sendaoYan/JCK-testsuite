/*
 * @test
 * @summary java.security.CryptoPrimitive conformance: white-box structure + black-box behavior.
 * @run main CryptoPrimitiveTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.CryptoPrimitive;

public class CryptoPrimitiveTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.CryptoPrimitive");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        checkEnumConstants(c, new String[]{"MESSAGE_DIGEST", "SECURE_RANDOM", "BLOCK_CIPHER", "STREAM_CIPHER", "MAC", "KEY_WRAP", "PUBLIC_KEY_ENCRYPTION", "SIGNATURE", "KEY_ENCAPSULATION", "KEY_AGREEMENT"});
        checkMethods(c, new String[]{"valueOf", "values"});
    }

    static void testBlackBox() throws Exception {
        check(CryptoPrimitive.valueOf("SIGNATURE") == CryptoPrimitive.SIGNATURE, "valueOf SIGNATURE");
        check(CryptoPrimitive.values().length == 10, "values length 10");
        for (CryptoPrimitive p : CryptoPrimitive.values()) {
            check(CryptoPrimitive.valueOf(p.name()) == p, "valueOf round-trip " + p.name());
        }
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
