/*
 * @test
 * @summary com.sun.nio.sctp.SctpServerChannel conformance: abstract channel.
 * @run main SctpServerChannelTest
 */

import java.lang.reflect.*;
import java.util.*;

public class SctpServerChannelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    public static void main(String[] a) throws Exception {
        Class<?> c = load("com.sun.nio.sctp.SctpServerChannel");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.nio.channels.spi.AbstractSelectableChannel"), "extends AbstractSelectableChannel");
        Set<String> m = methodNames(c);
        String[] names = {"open","accept","bind","bindAddress","unbindAddress","getAllLocalAddresses","getOption","setOption","supportedOptions","validOps"};
        for (String n : names) check(m.contains(n), n);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
