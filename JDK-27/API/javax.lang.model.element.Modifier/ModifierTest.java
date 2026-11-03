/*
 * @test
 * @summary javax.lang.model.element.Modifier conformance: white-box structure + black-box behavior.
 * @run main ModifierTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;

public class ModifierTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.lang.model.element.Modifier");
        check(c != null, "javax.lang.model.element.Modifier loads");
        check(c.getName().equals("javax.lang.model.element.Modifier"), "binary name is javax.lang.model.element.Modifier");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.Modifier is public");
        check(c.isEnum(), "is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 14, "14 enum constants");
        Set<String> names = new HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(Set.of("PUBLIC", "PROTECTED", "PRIVATE", "ABSTRACT", "DEFAULT", "STATIC", "SEALED", "NON_SEALED", "FINAL", "TRANSIENT", "VOLATILE", "SYNCHRONIZED", "NATIVE", "STRICTFP")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "PUBLIC") != null, "Enum.valueOf works");
        check(declares(c, "toString"), "declares toString()");
    }

    static void testBlackBox() throws Exception {
        Modifier[] vals = Modifier.values();
        check(vals.length == 14, "14 enum constants");
        check(Modifier.PUBLIC.toString().equals("public"), "PUBLIC toString public");
        check(Modifier.NON_SEALED.toString().equals("non-sealed"), "NON_SEALED toString non-sealed");
        check(Modifier.STRICTFP.toString().equals("strictfp"), "STRICTFP toString strictfp");
        check(Modifier.valueOf("PROTECTED") == Modifier.PROTECTED, "valueOf PROTECTED");
        check(Modifier.PRIVATE.ordinal() == 2, "PRIVATE ordinal 2");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
