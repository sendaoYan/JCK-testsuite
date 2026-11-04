/*
 * @test
 * @summary java.lang.WeakPairMap.Pair.Lookup conformance: white-box structure.
 *          (The enclosing WeakPairMap and its Pair interface are package-private, the
 *          constructor is package-private, and the static factories live on the
 *          inaccessible interface, so instances cannot be obtained from outside
 *          java.lang; coverage is therefore structural reflection.)
 * @run main LookupTest
 */
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class LookupTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.WeakPairMap$Pair$Lookup");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()),
              "public final class");
        boolean iface = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals("java.lang.WeakPairMap$Pair")) iface = true;
        }
        check(iface, "implements the (package-private) WeakPairMap$Pair interface");
        for (Constructor<?> ctor : c.getDeclaredConstructors()) {
            check(!Modifier.isPublic(ctor.getModifiers()),
                  "constructor is package-private (not instantiable outside java.lang)");
        }
        Set<String> names = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()), m.getName() + "() is public");
            names.add(m.getName());
        }
        check(names.contains("first"), "first() declared");
        check(names.contains("second"), "second() declared");
        check(names.contains("hashCode"), "hashCode() declared");
        check(names.contains("equals"), "equals() declared");
        check(c.getDeclaredMethod("first").getReturnType() == Object.class, "first() is generic <K1>");
        check(c.getDeclaredMethod("second").getReturnType() == Object.class, "second() is generic <K2>");
    }

    // ---- Part 2: black-box (behavioral) ----
    // WeakPairMap and WeakPairMap.Pair are package-private (javap: "final class
    // java.lang.WeakPairMap", "interface java.lang.WeakPairMap$Pair"), so the lookup()
    // factory is unreachable and no instance can be constructed from test code. Behavior
    // is exercised inside java.lang only (used by ThreadLocal internals).
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.WeakPairMap$Pair$Lookup");
        check(c.getClassLoader() == null, "loaded by the bootstrap loader");
        check(c.getPackageName().equals("java.lang"), "package java.lang");
        check(!c.isInterface() && !c.isEnum(), "plain class");
        check(c.getTypeParameters().length == 2, "generic in <K1, K2>");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
