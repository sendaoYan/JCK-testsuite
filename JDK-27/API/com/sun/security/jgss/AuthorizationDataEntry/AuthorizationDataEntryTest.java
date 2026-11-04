/*
 * @test
 * @summary com.sun.security.jgss.AuthorizationDataEntry conformance: white-box structure + black-box behavior.
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.security.jgss.AuthorizationDataEntry");
        check(!c.isInterface(), "concrete class");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        Set<String> m = methodNames(c);
        check(m.contains("getType"), "getType");
        check(m.contains("getData"), "getData");
        check(m.contains("toString"), "toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        byte[] data = new byte[]{1, 2, 3, 4};
        com.sun.security.jgss.AuthorizationDataEntry e = new com.sun.security.jgss.AuthorizationDataEntry(42, data);
        check(e.getType() == 42, "getType() == 42");

        byte[] got = e.getData();
        check(Arrays.equals(got, data), "getData() content equals input");
        check(got != data, "getData() returns a copy, not the same array");

        // Mutating the returned array must not affect the internal state.
        got[0] = 99;
        check(Arrays.equals(e.getData(), new byte[]{1, 2, 3, 4}), "internal data not mutated via returned array");

        // The constructor must copy its input as well.
        data[0] = 55;
        check(Arrays.equals(e.getData(), new byte[]{1, 2, 3, 4}), "constructor copied input");

        check(e.getData() != e.getData(), "getData() returns a fresh copy each call");

        // No type validation: zero and negative types are accepted.
        check(new com.sun.security.jgss.AuthorizationDataEntry(0, new byte[]{7}).getType() == 0, "type 0 accepted");
        check(new com.sun.security.jgss.AuthorizationDataEntry(-1, new byte[]{7}).getType() == -1, "negative type accepted");

        // Empty payload.
        check(new com.sun.security.jgss.AuthorizationDataEntry(1, new byte[0]).getData().length == 0, "empty data");

        String s = new com.sun.security.jgss.AuthorizationDataEntry(7, new byte[]{9, 8}).toString();
        check(s != null && s.contains("type=7"), "toString() mentions type");

        boolean npe = false;
        try { new com.sun.security.jgss.AuthorizationDataEntry(1, null); }
        catch (NullPointerException ex) { npe = true; }
        check(npe, "null data -> NullPointerException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
