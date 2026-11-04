/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.WalkerFactory conformance: reflection.
 * @run main WalkerFactoryTest
 */

public class WalkerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.WalkerFactory", false, WalkerFactoryTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("WalkerFactory"), "simple name WalkerFactory");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "isSet"), "method isSet");
        check(has(c, "diagnoseIterator"), "method diagnoseIterator");
        check(has(c, "newDTMIterator"), "method newDTMIterator");
        check(has(c, "getAxisFromStep"), "method getAxisFromStep");
        check(has(c, "getAnalysisBitFromAxes"), "method getAnalysisBitFromAxes");
        check(has(c, "mightBeProximate"), "method mightBeProximate");
        check(has(c, "isDownwardAxisOfMany"), "method isDownwardAxisOfMany");
        check(has(c, "getAnalysisString"), "method getAnalysisString");
        check(has(c, "hasPredicate"), "method hasPredicate");
        check(has(c, "isWild"), "method isWild");
        check(has(c, "walksAncestors"), "method walksAncestors");
        check(has(c, "walksAttributes"), "method walksAttributes");
        check(has(c, "walksNamespaces"), "method walksNamespaces");
        check(has(c, "walksChildren"), "method walksChildren");
        check(has(c, "walksDescendants"), "method walksDescendants");
        check(has(c, "walksSubtree"), "method walksSubtree");
        check(has(c, "walksSubtreeOnlyMaybeAbsolute"), "method walksSubtreeOnlyMaybeAbsolute");
        check(has(c, "walksSubtreeOnly"), "method walksSubtreeOnly");
        check(has(c, "walksFilteredList"), "method walksFilteredList");
        check(has(c, "walksSubtreeOnlyFromRootOrContext"), "method walksSubtreeOnlyFromRootOrContext");
        check(has(c, "walksInDocOrder"), "method walksInDocOrder");
        check(has(c, "walksFollowingOnlyMaybeAbsolute"), "method walksFollowingOnlyMaybeAbsolute");
        check(has(c, "walksUp"), "method walksUp");
        check(has(c, "walksSideways"), "method walksSideways");
        check(has(c, "walksExtraNodes"), "method walksExtraNodes");
        check(has(c, "walksExtraNodesOnly"), "method walksExtraNodesOnly");
        check(has(c, "isAbsolute"), "method isAbsolute");
        check(has(c, "walksChildrenOnly"), "method walksChildrenOnly");
        check(has(c, "walksChildrenAndExtraAndSelfOnly"), "method walksChildrenAndExtraAndSelfOnly");
        check(has(c, "walksDescendantsAndExtraAndSelfOnly"), "method walksDescendantsAndExtraAndSelfOnly");
        check(has(c, "walksSelfOnly"), "method walksSelfOnly");
        check(has(c, "walksUpOnly"), "method walksUpOnly");
        check(has(c, "walksDownOnly"), "method walksDownOnly");
        check(has(c, "walksDownExtraOnly"), "method walksDownExtraOnly");
        check(has(c, "canSkipSubtrees"), "method canSkipSubtrees");
        check(has(c, "canCrissCross"), "method canCrissCross");
        check(has(c, "isNaturalDocOrder"), "method isNaturalDocOrder");
        check(has(c, "isOneStep"), "method isOneStep");
        check(has(c, "getStepCount"), "method getStepCount");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
