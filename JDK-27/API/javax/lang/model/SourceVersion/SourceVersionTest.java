/*
 * @test
 * @summary javax.lang.model.SourceVersion conformance: white-box structure + black-box behavior.
 * @run main SourceVersionTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;

public class SourceVersionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.lang.model.SourceVersion");
        check(c != null, "javax.lang.model.SourceVersion loads");
        check(c.getName().equals("javax.lang.model.SourceVersion"), "binary name is javax.lang.model.SourceVersion");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.SourceVersion is public");
        check(c.isEnum(), "is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 28, "28 enum constants");
        Set<String> names = new HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(Set.of("RELEASE_0", "RELEASE_1", "RELEASE_2", "RELEASE_3", "RELEASE_4", "RELEASE_5", "RELEASE_6", "RELEASE_7", "RELEASE_8", "RELEASE_9", "RELEASE_10", "RELEASE_11", "RELEASE_12", "RELEASE_13", "RELEASE_14", "RELEASE_15", "RELEASE_16", "RELEASE_17", "RELEASE_18", "RELEASE_19", "RELEASE_20", "RELEASE_21", "RELEASE_22", "RELEASE_23", "RELEASE_24", "RELEASE_25", "RELEASE_26", "RELEASE_27")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "RELEASE_0") != null, "Enum.valueOf works");
        check(declares(c, "latest"), "declares latest()");
        check(declares(c, "latestSupported"), "declares latestSupported()");
        check(declares(c, "isIdentifier"), "declares isIdentifier()");
        check(declares(c, "isName"), "declares isName()");
        check(declares(c, "isKeyword"), "declares isKeyword()");
        check(declares(c, "runtimeVersion"), "declares runtimeVersion()");
    }

    static void testBlackBox() throws Exception {
        SourceVersion latest = SourceVersion.latest();
        check(latest == SourceVersion.latestSupported(), "latest == latestSupported");
        check(latest.name().equals("RELEASE_" + Runtime.version().feature()), "latest matches runtime feature");
        check(latest.runtimeVersion().feature() == Runtime.version().feature(), "runtimeVersion feature");
        check(SourceVersion.valueOf("RELEASE_8").ordinal() < SourceVersion.valueOf("RELEASE_27").ordinal(), "RELEASE_8 < RELEASE_27");
        check(SourceVersion.isIdentifier("foo"), "isIdentifier foo");
        check(!SourceVersion.isIdentifier("123abc"), "!isIdentifier 123abc");
        check(SourceVersion.isName("foo.bar.Baz"), "isName qualified");
        check(!SourceVersion.isName("class"), "!isName keyword");
        check(SourceVersion.isKeyword("class") && SourceVersion.isKeyword("int"), "isKeyword class/int");
        check(!SourceVersion.isKeyword("foobar"), "!isKeyword foobar");
        check(SourceVersion.valueOf(Runtime.version()) == latest, "valueOf(Runtime.Version) == latest");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
