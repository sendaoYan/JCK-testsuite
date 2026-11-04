/*
 * @test
 * @summary javax.lang.model.element.NestingKind conformance: white-box structure + black-box behavior.
 * @run main NestingKindTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;

public class NestingKindTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.lang.model.element.NestingKind");
        check(c != null, "javax.lang.model.element.NestingKind loads");
        check(c.getName().equals("javax.lang.model.element.NestingKind"), "binary name is javax.lang.model.element.NestingKind");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.NestingKind is public");
        check(c.isEnum(), "is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 4, "4 enum constants");
        Set<String> names = new HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(Set.of("TOP_LEVEL", "MEMBER", "LOCAL", "ANONYMOUS")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "TOP_LEVEL") != null, "Enum.valueOf works");
        check(declares(c, "isNested"), "declares isNested()");
    }

    static void testBlackBox() throws Exception {
        NestingKind[] vals = NestingKind.values();
        check(vals.length == 4, "4 enum constants");
        check(!NestingKind.TOP_LEVEL.isNested(), "TOP_LEVEL !isNested");
        check(NestingKind.MEMBER.isNested() && NestingKind.LOCAL.isNested() && NestingKind.ANONYMOUS.isNested(), "MEMBER/LOCAL/ANONYMOUS isNested");
        check(NestingKind.valueOf("LOCAL") == NestingKind.LOCAL, "valueOf LOCAL");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
