/*
 * @test
 * @summary java.security.KeyRep.Type conformance: white-box structure + black-box behavior.
 * @run main TypeTest
 */

import java.lang.reflect.Modifier;
import java.security.KeyRep;
import java.util.Arrays;

public class TypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyRep$Type");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        checkEnumConstants(c, new String[]{"SECRET", "PUBLIC", "PRIVATE"});
        checkMethods(c, new String[]{"valueOf", "values"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyRep.Type[] values = KeyRep.Type.values();
        check(values.length == 3, "values length 3");

        check(KeyRep.Type.valueOf("SECRET") == KeyRep.Type.SECRET, "valueOf SECRET");
        check(KeyRep.Type.valueOf("PUBLIC") == KeyRep.Type.PUBLIC, "valueOf PUBLIC");
        check(KeyRep.Type.valueOf("PRIVATE") == KeyRep.Type.PRIVATE, "valueOf PRIVATE");

        // name() round-trip
        for (KeyRep.Type t : values) {
            check(KeyRep.Type.valueOf(t.name()) == t, "name/valueOf roundtrip " + t.name());
        }

        // ordinal ordering stable (declaration order SECRET, PUBLIC, PRIVATE)
        check(KeyRep.Type.SECRET.ordinal() == 0, "SECRET ordinal 0");
        check(KeyRep.Type.PUBLIC.ordinal() == 1, "PUBLIC ordinal 1");
        check(KeyRep.Type.PRIVATE.ordinal() == 2, "PRIVATE ordinal 2");

        // ---- appended contract dimensions ----
        // compareTo consistent with ordinals
        check(KeyRep.Type.SECRET.compareTo(KeyRep.Type.PUBLIC) < 0, "SECRET < PUBLIC");
        check(KeyRep.Type.PRIVATE.compareTo(KeyRep.Type.SECRET) > 0, "PRIVATE > SECRET");
        check(KeyRep.Type.SECRET.compareTo(KeyRep.Type.SECRET) == 0, "compareTo self 0");
        // name()/toString() agree; equals identity semantics
        for (KeyRep.Type t : values) check(t.toString().equals(t.name()), "toString==name " + t.name());
        check(!KeyRep.Type.SECRET.equals(KeyRep.Type.PUBLIC), "distinct constants unequal");
        check(KeyRep.Type.SECRET.hashCode() == KeyRep.Type.SECRET.hashCode(), "hashCode stable");
        // values() returns a fresh array each call
        KeyRep.Type[] a1 = KeyRep.Type.values();
        KeyRep.Type[] a2 = KeyRep.Type.values();
        check(a1 != a2, "values() fresh array");
        check(Arrays.equals(a1, a2), "values() same content");
        // getDeclaringClass is the enum class itself
        check(KeyRep.Type.SECRET.getDeclaringClass() == KeyRep.Type.class, "getDeclaringClass");
        check(KeyRep.Type.class.getEnumConstants().length == 3, "getEnumConstants length");
        // bogus / case-mismatched / null names rejected
        try { KeyRep.Type.valueOf("secret"); check(false, "valueOf case-sensitive"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("secret"), "IAE message"); }
        try { KeyRep.Type.valueOf("NO_SUCH"); check(false, "valueOf bogus IAE"); }
        catch (IllegalArgumentException e) { passed++; }
        try { KeyRep.Type.valueOf(null); check(false, "valueOf null NPE"); }
        catch (NullPointerException e) { passed++; }
        // enum serialization restores the singleton constant
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(KeyRep.Type.PUBLIC);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(ois.readObject() == KeyRep.Type.PUBLIC, "serialization restores singleton");
        // EnumSet interop
        java.util.EnumSet<KeyRep.Type> set = java.util.EnumSet.of(KeyRep.Type.SECRET, KeyRep.Type.PRIVATE);
        check(set.size() == 2 && !set.contains(KeyRep.Type.PUBLIC), "EnumSet contains");
        check(java.util.EnumSet.allOf(KeyRep.Type.class).size() == 3, "EnumSet allOf size");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
