/*
 * @test
 * @summary java.lang.WeakPairMap.Pair.Lookup conformance: public nested class reflection.
 * @run main LookupTest
 */
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class LookupTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.lang.WeakPairMap$Pair$Lookup");
        check(c != null, "loadable");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()), "public final");
        boolean iface = false;
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals("java.lang.WeakPairMap$Pair")) iface = true;
        check(iface, "implements WeakPairMap$Pair");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("first"), "first()");
        check(names.contains("second"), "second()");
        check(names.contains("hashCode"), "hashCode()");
        check(names.contains("equals"), "equals()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
