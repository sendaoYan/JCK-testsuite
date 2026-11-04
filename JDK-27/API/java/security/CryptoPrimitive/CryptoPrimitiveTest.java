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

        // ---- appended contract dimensions ----
        CryptoPrimitive[] vs = CryptoPrimitive.values();
        // declaration order fixes ordinals 0..9
        // (MESSAGE_DIGEST, SECURE_RANDOM, BLOCK_CIPHER, STREAM_CIPHER, MAC, KEY_WRAP,
        //  PUBLIC_KEY_ENCRYPTION, SIGNATURE, KEY_ENCAPSULATION, KEY_AGREEMENT)
        for (int i = 0; i < vs.length; i++) check(vs[i].ordinal() == i, "ordinal " + vs[i].name() + "==" + i);
        check(vs[5] == CryptoPrimitive.KEY_WRAP, "declaration order: KEY_WRAP is 5th");
        check(vs[7] == CryptoPrimitive.SIGNATURE, "declaration order: SIGNATURE is 8th");
        // compareTo consistent with ordinals
        check(CryptoPrimitive.MESSAGE_DIGEST.compareTo(CryptoPrimitive.SIGNATURE) < 0, "MD < SIGNATURE");
        check(CryptoPrimitive.KEY_AGREEMENT.compareTo(CryptoPrimitive.BLOCK_CIPHER) > 0, "KA > BC");
        check(CryptoPrimitive.MAC.compareTo(CryptoPrimitive.MAC) == 0, "compareTo self zero");
        // name/toString agreement; identity equals
        for (CryptoPrimitive p : vs) check(p.toString().equals(p.name()), "toString==name " + p.name());
        check(!CryptoPrimitive.MAC.equals(CryptoPrimitive.SIGNATURE), "distinct unequal");
        // values() fresh array each call
        CryptoPrimitive[] a1 = CryptoPrimitive.values(), a2 = CryptoPrimitive.values();
        check(a1 != a2, "values() fresh array");
        check(java.util.Arrays.equals(a1, a2), "values() same content");
        // getDeclaringClass / getEnumConstants
        check(CryptoPrimitive.SIGNATURE.getDeclaringClass() == CryptoPrimitive.class, "getDeclaringClass");
        check(CryptoPrimitive.class.getEnumConstants().length == 10, "getEnumConstants");
        // bogus / null rejected
        try { CryptoPrimitive.valueOf("signature"); check(false, "valueOf case-sensitive IAE"); }
        catch (IllegalArgumentException e) { passed++; }
        try { CryptoPrimitive.valueOf("NO_SUCH"); check(false, "valueOf bogus IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("NO_SUCH"), "IAE message"); }
        try { CryptoPrimitive.valueOf(null); check(false, "valueOf null NPE"); }
        catch (NullPointerException e) { passed++; }
        // serialization restores the singleton constant
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(CryptoPrimitive.SIGNATURE);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(ois.readObject() == CryptoPrimitive.SIGNATURE, "serialization restores singleton");
        // EnumSet usage per javadoc
        java.util.EnumSet<CryptoPrimitive> digests = java.util.EnumSet.of(CryptoPrimitive.MESSAGE_DIGEST);
        java.util.EnumSet<CryptoPrimitive> sym = java.util.EnumSet.of(CryptoPrimitive.BLOCK_CIPHER,
                CryptoPrimitive.STREAM_CIPHER);
        check(digests.size() == 1 && sym.size() == 2, "EnumSet sizes");
        check(java.util.Collections.disjoint(digests, sym), "disjoint groups");
        check(java.util.EnumSet.allOf(CryptoPrimitive.class).size() == 10, "allOf size");
        check(java.util.EnumSet.range(CryptoPrimitive.BLOCK_CIPHER, CryptoPrimitive.MAC).size() == 3,
                "range BLOCK_CIPHER..MAC size 3");
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
