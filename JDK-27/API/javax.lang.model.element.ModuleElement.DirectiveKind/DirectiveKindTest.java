/*
 * @test
 * @summary javax.lang.model.element.ModuleElement.DirectiveKind conformance: white-box structure + black-box behavior.
 * @run main DirectiveKindTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;

public class DirectiveKindTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.lang.model.element.ModuleElement$DirectiveKind");
        check(c != null, "javax.lang.model.element.ModuleElement$DirectiveKind loads");
        check(c.getName().equals("javax.lang.model.element.ModuleElement$DirectiveKind"), "binary name is javax.lang.model.element.ModuleElement$DirectiveKind");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.ModuleElement$DirectiveKind is public");
        check(c.isEnum(), "is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 5, "5 enum constants");
        Set<String> names = new HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(Set.of("REQUIRES", "EXPORTS", "OPENS", "USES", "PROVIDES")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "REQUIRES") != null, "Enum.valueOf works");
    }

    static void testBlackBox() throws Exception {
        ModuleElement.DirectiveKind[] vals = ModuleElement.DirectiveKind.values();
        check(vals.length == 5, "5 enum constants");
        check(ModuleElement.DirectiveKind.REQUIRES.ordinal() == 0, "REQUIRES ordinal 0");
        check(ModuleElement.DirectiveKind.PROVIDES.ordinal() == 4, "PROVIDES ordinal 4");
        check(ModuleElement.DirectiveKind.valueOf("EXPORTS") == ModuleElement.DirectiveKind.EXPORTS, "valueOf EXPORTS");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
