/*
 * @test
 * @summary java.net.StandardProtocolFamily conformance: enum structure
 *          (implements ProtocolFamily, Enum superclass), the exact JDK-8
 *          constant set {INET, INET6} (JDK 16 adds UNIX), name()/toString()
 *          exact strings, valueOf round-trips plus IAE/NPE for bad names,
 *          ordinal and compareTo ordering, equals/hashCode/identity,
 *          serialization identity of enum constants, and values() clone
 *          independence.
 * @run main StandardProtocolFamilyTest
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ProtocolFamily;
import java.net.StandardProtocolFamily;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StandardProtocolFamilyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.StandardProtocolFamily");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        Set<String> expIfs = new HashSet<String>();
        expIfs.add("java.net.ProtocolFamily");
        Set<String> actIfs = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        // Enum members present.
        check(c.getMethod("values") != null, "values()");
        check(c.getMethod("valueOf", String.class) != null, "valueOf(String)");
        Method name = c.getMethod("name");
        check(name != null, "name() from ProtocolFamily");
        check(name.getReturnType() == String.class, "name returns String");
    }

    static void testConstants() throws Exception {
        Class<?> c = Class.forName("java.net.StandardProtocolFamily");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "enum constant count");
        check(((java.lang.Enum<?>) ec[0]).name().equals("INET"), "constant INET");
        check(((java.lang.Enum<?>) ec[1]).name().equals("INET6"), "constant INET6");
        check(Arrays.asList(ec).contains(StandardProtocolFamily.INET), "values contains INET");
        check(Arrays.asList(ec).contains(StandardProtocolFamily.INET6), "values contains INET6");
        // exact name strings
        check("INET".equals(StandardProtocolFamily.INET.name()), "INET name");
        check("INET6".equals(StandardProtocolFamily.INET6.name()), "INET6 name");
        // toString mirrors name for enum constants
        check("INET".equals(StandardProtocolFamily.INET.toString()), "INET toString");
        check("INET6".equals(StandardProtocolFamily.INET6.toString()), "INET6 toString");
        // implements ProtocolFamily
        check(StandardProtocolFamily.INET instanceof ProtocolFamily, "INET implements ProtocolFamily");
        check(StandardProtocolFamily.INET6 instanceof ProtocolFamily, "INET6 implements ProtocolFamily");
        ProtocolFamily pf = StandardProtocolFamily.INET;
        check("INET".equals(pf.name()), "ProtocolFamily.name dispatch");
    }

    static void testValueOf() throws Exception {
        check(StandardProtocolFamily.valueOf("INET") == StandardProtocolFamily.INET, "valueOf INET");
        check(StandardProtocolFamily.valueOf("INET6") == StandardProtocolFamily.INET6, "valueOf INET6");
        try {
            StandardProtocolFamily.valueOf("UNIX");
            check(false, "UNIX must not exist on JDK-8");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("UNIX"),
                    "UNIX absent message, got: " + e.getMessage());
        }
        try {
            StandardProtocolFamily.valueOf("BOGUS");
            check(false, "valueOf bogus must fail");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            StandardProtocolFamily.valueOf("");
            check(false, "valueOf empty must fail");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            StandardProtocolFamily.valueOf(null);
            check(false, "valueOf(null) must fail");
        } catch (NullPointerException e) {
            passed++;
        }
        // Case-sensitive lookup.
        try {
            StandardProtocolFamily.valueOf("inet");
            check(false, "valueOf lowercase must fail");
        } catch (IllegalArgumentException e) {
            passed++;
        }
    }

    static void testEnumContract() throws Exception {
        check(StandardProtocolFamily.INET.ordinal() == 0, "INET ordinal 0");
        check(StandardProtocolFamily.INET6.ordinal() == 1, "INET6 ordinal 1");
        check(StandardProtocolFamily.INET.compareTo(StandardProtocolFamily.INET6) < 0,
                "INET < INET6");
        check(StandardProtocolFamily.INET6.compareTo(StandardProtocolFamily.INET) > 0,
                "INET6 > INET");
        check(StandardProtocolFamily.INET.compareTo(StandardProtocolFamily.INET) == 0,
                "self compareTo 0");
        check(StandardProtocolFamily.INET.equals(StandardProtocolFamily.INET), "equals self");
        check(!StandardProtocolFamily.INET.equals(StandardProtocolFamily.INET6), "not equals other");
        check(StandardProtocolFamily.INET.hashCode() == StandardProtocolFamily.INET.hashCode(),
                "hashCode stable");
        check(StandardProtocolFamily.INET.getDeclaringClass() == StandardProtocolFamily.class,
                "declaring class");
        // values() returns a fresh array each call.
        StandardProtocolFamily[] a1 = StandardProtocolFamily.values();
        StandardProtocolFamily[] a2 = StandardProtocolFamily.values();
        check(a1 != a2, "values returns clone");
        check(Arrays.equals(a1, a2), "clones equal content");
    }

    static void testSerialization() throws Exception {
        // Enum constants serialize to the identical singleton.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(StandardProtocolFamily.INET6);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        check(back == StandardProtocolFamily.INET6, "serialization identity");
        check(back instanceof ProtocolFamily, "deserialized implements ProtocolFamily");
        check(((ProtocolFamily) back).name().equals("INET6"), "deserialized name");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.StandardProtocolFamily");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "enum constant count");
        check(((java.lang.Enum<?>) ec[0]).name().equals("INET"), "constant INET");
        check(((java.lang.Enum<?>) ec[1]).name().equals("INET6"), "constant INET6");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.net.ProtocolFamily");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        testWhiteBox();
        testConstants();
        testValueOf();
        testEnumContract();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
