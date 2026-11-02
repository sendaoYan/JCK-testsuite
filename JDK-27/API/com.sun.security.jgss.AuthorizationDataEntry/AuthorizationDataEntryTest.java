/*
 * @test
 * @summary com.sun.security.jgss.AuthorizationDataEntry conformance: accessors.
 * @run main AuthorizationDataEntryTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AuthorizationDataEntryTest {
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
        byte[] data = new byte[]{1,2,3};
        com.sun.security.jgss.AuthorizationDataEntry e = new com.sun.security.jgss.AuthorizationDataEntry(1, data);
        check(e.getType() == 1, "type");
        check(java.util.Arrays.equals(e.getData(), data), "data");
        check(e.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
