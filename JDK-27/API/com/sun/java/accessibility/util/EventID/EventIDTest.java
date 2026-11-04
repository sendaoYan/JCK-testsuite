/*
 * @test
 * @summary com.sun.java.accessibility.util.EventID conformance: white-box structure + black-box behavior.
 * @run main EventIDTest
 */

import java.lang.reflect.*;
import java.util.*;

public class EventIDTest {
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
        Class<?> c = load("com.sun.java.accessibility.util.EventID");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        Set<String> f = fieldNames(c);
        String[] names = {"ACTION","ADJUSTMENT","COMPONENT","CONTAINER","FOCUS","ITEM","KEY","MOUSE","MOTION","TEXT","WINDOW","PROPERTYCHANGE"};
        for (String n : names) check(f.contains(n), "field " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = load("com.sun.java.accessibility.util.EventID");
        // Full set of 29 documented constants (javap + jdk25u-dev EventID.java).
        String[] all = {"ACTION","ADJUSTMENT","COMPONENT","CONTAINER","FOCUS","ITEM","KEY","MOUSE","MOTION","TEXT","WINDOW","ANCESTOR","CARET","CELLEDITOR","CHANGE","COLUMNMODEL","DOCUMENT","LISTDATA","LISTSELECTION","MENU","POPUPMENU","TABLEMODEL","TREEEXPANSION","TREEMODEL","TREESELECTION","UNDOABLEEDIT","PROPERTYCHANGE","VETOABLECHANGE","INTERNALFRAME"};
        int[] expected = {0,1,2,3,4,5,6,7,8,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29};
        Set<Integer> distinct = new HashSet<>();
        for (int i = 0; i < all.length; i++) {
            Field fd = c.getField(all[i]);
            int mods = fd.getModifiers();
            check(Modifier.isPublic(mods) && Modifier.isStatic(mods) && Modifier.isFinal(mods), all[i] + " public static final");
            check(fd.getType() == int.class, all[i] + " is int");
            int v = fd.getInt(null);
            check(v >= 0, all[i] + " non-negative");
            check(v == expected[i], all[i] + " == " + expected[i]);
            check(distinct.add(v), all[i] + " distinct value");
        }
        check(all.length == 29, "29 constants total");
        // Instantiation must not throw.
        com.sun.java.accessibility.util.EventID eid = new com.sun.java.accessibility.util.EventID();
        check(eid != null, "new EventID() non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
