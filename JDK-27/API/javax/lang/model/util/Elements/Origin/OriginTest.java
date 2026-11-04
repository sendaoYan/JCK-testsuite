/*
 * @test
 * @summary javax.lang.model.util.Elements.Origin conformance: white-box structure + black-box behavior.
 * @run main OriginTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;

public class OriginTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.lang.model.util.Elements$Origin");
        check(c != null, "javax.lang.model.util.Elements$Origin loads");
        check(c.getName().equals("javax.lang.model.util.Elements$Origin"), "binary name is javax.lang.model.util.Elements$Origin");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.Elements$Origin is public");
        check(c.isEnum(), "is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 3, "3 enum constants");
        Set<String> names = new HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(Set.of("EXPLICIT", "MANDATED", "SYNTHETIC")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "EXPLICIT") != null, "Enum.valueOf works");
        check(declares(c, "isDeclared"), "declares isDeclared()");
    }

    static void testBlackBox() throws Exception {
        Elements.Origin[] vals = Elements.Origin.values();
        check(vals.length == 3, "3 enum constants");
        check(Elements.Origin.EXPLICIT.isDeclared(), "EXPLICIT isDeclared");
        check(Elements.Origin.MANDATED.isDeclared(), "MANDATED isDeclared");
        check(!Elements.Origin.SYNTHETIC.isDeclared(), "SYNTHETIC !isDeclared");
        check(Elements.Origin.valueOf("MANDATED") == Elements.Origin.MANDATED, "valueOf MANDATED");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
