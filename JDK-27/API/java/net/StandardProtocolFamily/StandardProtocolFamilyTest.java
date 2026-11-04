/*
 * @test
 * @summary java.net.StandardProtocolFamily conformance: white-box structure + black-box behavior.
 *          Deepened: exact constant set {INET, INET6, UNIX} (UNIX added in JDK 16),
 *          name()/toString() exact strings, valueOf round-trips plus IAE/NPE for
 *          bad names, ordinal and compareTo ordering, equals/hashCode/identity,
 *          serialization identity of enum constants, values() clone independence,
 *          and use as ProtocolFamily in channel opening.
 * @run main StandardProtocolFamilyTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class StandardProtocolFamilyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.StandardProtocolFamily");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.isEnum(), "enum");
        check(ProtocolFamily.class.isAssignableFrom(c), "implements ProtocolFamily");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        Set<String> expIfs = new HashSet<>();
        expIfs.add("java.net.ProtocolFamily");
        Set<String> actIfs = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        check(c.getMethod("values") != null, "values()");
        check(c.getMethod("valueOf", String.class) != null, "valueOf(String)");
        Method name = c.getMethod("name");
        check(name != null, "name() from ProtocolFamily");
        check(name.getReturnType() == String.class, "name returns String");
    }

    static void testBlackBox() throws Exception {
        StandardProtocolFamily[] vals = StandardProtocolFamily.values();
        check(vals.length == 3, "3 constants");
        List<StandardProtocolFamily> list = Arrays.asList(vals);
        check(list.contains(StandardProtocolFamily.INET), "INET present");
        check(list.contains(StandardProtocolFamily.INET6), "INET6 present");
        check(list.contains(StandardProtocolFamily.UNIX), "UNIX present");

        check(StandardProtocolFamily.INET.name().equals("INET"), "INET name");
        check(StandardProtocolFamily.INET6.name().equals("INET6"), "INET6 name");
        check(StandardProtocolFamily.UNIX.name().equals("UNIX"), "UNIX name");

        check(StandardProtocolFamily.valueOf("INET") == StandardProtocolFamily.INET, "valueOf INET");
        check(StandardProtocolFamily.valueOf("UNIX") == StandardProtocolFamily.UNIX, "valueOf UNIX");

        // implements ProtocolFamily.name()
        ProtocolFamily pf = StandardProtocolFamily.INET;
        check(pf.name().equals("INET"), "ProtocolFamily.name");

        // enum identity: valueOf roundtrip and ordinal ordering
        check(StandardProtocolFamily.INET.ordinal() == 0, "INET ordinal 0");
        check(Arrays.asList(vals).indexOf(StandardProtocolFamily.INET6) == 1, "INET6 ordinal 1");

        fails(IllegalArgumentException.class, "valueOf bogus", () -> StandardProtocolFamily.valueOf("BOGUS"));
    }

    static void testNamesAndToString() throws Exception {
        check("INET".equals(StandardProtocolFamily.INET.toString()), "INET toString");
        check("INET6".equals(StandardProtocolFamily.INET6.toString()), "INET6 toString");
        check("UNIX".equals(StandardProtocolFamily.UNIX.toString()), "UNIX toString");
        check(StandardProtocolFamily.INET instanceof ProtocolFamily, "INET implements ProtocolFamily");
        check(StandardProtocolFamily.INET6 instanceof ProtocolFamily, "INET6 implements ProtocolFamily");
        check(StandardProtocolFamily.UNIX instanceof ProtocolFamily, "UNIX implements ProtocolFamily");
        ProtocolFamily inet6 = StandardProtocolFamily.INET6;
        check("INET6".equals(inet6.name()), "INET6 name via interface");
        ProtocolFamily unix = StandardProtocolFamily.UNIX;
        check("UNIX".equals(unix.name()), "UNIX name via interface");
        // String concatenation uses toString == name.
        check("family=UNIX".equals("family=" + StandardProtocolFamily.UNIX), "concat uses name");
    }

    static void testValueOfEdges() throws Exception {
        check(StandardProtocolFamily.valueOf("INET6") == StandardProtocolFamily.INET6, "valueOf INET6");
        fails(IllegalArgumentException.class, "valueOf empty", () -> StandardProtocolFamily.valueOf(""));
        fails(NullPointerException.class, "valueOf null", () -> StandardProtocolFamily.valueOf(null));
        fails(IllegalArgumentException.class, "valueOf lowercase", () -> StandardProtocolFamily.valueOf("unix"));
        fails(IllegalArgumentException.class, "valueOf mixed", () -> StandardProtocolFamily.valueOf("Inet"));
        try {
            StandardProtocolFamily.valueOf("BOGUS");
            check(false, "valueOf bogus must fail");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("BOGUS"),
                    "IAE message names the constant, got: " + e.getMessage());
        }
    }

    static void testEnumContract() throws Exception {
        check(StandardProtocolFamily.UNIX.ordinal() == 2, "UNIX ordinal 2");
        check(StandardProtocolFamily.INET.compareTo(StandardProtocolFamily.INET6) < 0, "INET < INET6");
        check(StandardProtocolFamily.INET6.compareTo(StandardProtocolFamily.UNIX) < 0, "INET6 < UNIX");
        check(StandardProtocolFamily.UNIX.compareTo(StandardProtocolFamily.INET) > 0, "UNIX > INET");
        check(StandardProtocolFamily.INET6.compareTo(StandardProtocolFamily.INET6) == 0, "self compareTo 0");
        check(StandardProtocolFamily.INET.equals(StandardProtocolFamily.INET), "equals self");
        check(!StandardProtocolFamily.INET.equals(StandardProtocolFamily.UNIX), "not equals other");
        check(!StandardProtocolFamily.UNIX.equals(null), "not equals null");
        check(StandardProtocolFamily.UNIX.hashCode() == StandardProtocolFamily.UNIX.hashCode(), "hashCode stable");
        check(StandardProtocolFamily.INET.getDeclaringClass() == StandardProtocolFamily.class, "declaring class");
        StandardProtocolFamily[] a1 = StandardProtocolFamily.values();
        StandardProtocolFamily[] a2 = StandardProtocolFamily.values();
        check(a1 != a2, "values returns clone");
        check(Arrays.equals(a1, a2), "clones equal content");
        // Mutating a returned array must not corrupt the enum.
        a1[0] = StandardProtocolFamily.UNIX;
        check(StandardProtocolFamily.values()[0] == StandardProtocolFamily.INET, "values immune to mutation");
    }

    static void testSerialization() throws Exception {
        // Enum constants serialize to the identical singleton.
        for (StandardProtocolFamily v : StandardProtocolFamily.values()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(v);
            oos.close();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            Object back = ois.readObject();
            check(back == v, "serialization identity " + v.name());
            check(back instanceof ProtocolFamily, "deserialized implements ProtocolFamily");
            check(((ProtocolFamily) back).name().equals(v.name()), "deserialized name " + v.name());
        }
    }

    static void testChannelUsage() throws Exception {
        // ProtocolFamily drives channel family selection on JDK 16+.
        java.nio.channels.SocketChannel inet =
                java.nio.channels.SocketChannel.open(StandardProtocolFamily.INET);
        check(inet != null, "SocketChannel.open(INET)");
        inet.close();
        check(!inet.isOpen(), "INET channel closed");
        java.nio.channels.ServerSocketChannel unix =
                java.nio.channels.ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        check(unix != null, "ServerSocketChannel.open(UNIX)");
        unix.close();
        check(!unix.isOpen(), "UNIX channel closed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNamesAndToString();
        testValueOfEdges();
        testEnumContract();
        testSerialization();
        testChannelUsage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
