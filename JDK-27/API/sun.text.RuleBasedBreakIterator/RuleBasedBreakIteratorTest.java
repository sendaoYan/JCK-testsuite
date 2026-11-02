/*
 * @test
 * @summary sun.text.RuleBasedBreakIterator conformance: internal package reflection.
 * @modules java.base/sun.text:+open
 * @run main RuleBasedBreakIteratorTest
 */
public class RuleBasedBreakIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.RuleBasedBreakIterator", false, RuleBasedBreakIteratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.text.BreakIterator"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "checkOffset"), "method checkOffset");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "current"), "method current");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "first"), "method first");
        check(hasMethod(c, "following"), "method following");
        check(hasMethod(c, "getText"), "method getText");
        check(hasMethod(c, "handleNext"), "method handleNext");
        check(hasMethod(c, "handlePrevious"), "method handlePrevious");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isBoundary"), "method isBoundary");
        check(hasMethod(c, "last"), "method last");
        check(hasMethod(c, "lookupBackwardState"), "method lookupBackwardState");
        check(hasMethod(c, "lookupCategory"), "method lookupCategory");
        check(hasMethod(c, "lookupState"), "method lookupState");
        check(hasMethod(c, "next"), "method next");
        check(hasMethod(c, "preceding"), "method preceding");
        check(hasMethod(c, "previous"), "method previous");
        check(hasMethod(c, "setText"), "method setText");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "BMP_INDICES_LENGTH"), "field BMP_INDICES_LENGTH");
        check(hasField(c, "IGNORE"), "field IGNORE");
        check(hasField(c, "LABEL"), "field LABEL");
        check(hasField(c, "LABEL_LENGTH"), "field LABEL_LENGTH");
        check(hasField(c, "START_STATE"), "field START_STATE");
        check(hasField(c, "STOP_STATE"), "field STOP_STATE");
        check(hasField(c, "additionalData"), "field additionalData");
        check(hasField(c, "backwardsStateTable"), "field backwardsStateTable");
        check(hasField(c, "cachedLastKnownBreak"), "field cachedLastKnownBreak");
        check(hasField(c, "charCategoryTable"), "field charCategoryTable");
        check(hasField(c, "checksum"), "field checksum");
        check(hasField(c, "endStates"), "field endStates");
        check(hasField(c, "lookaheadStates"), "field lookaheadStates");
        check(hasField(c, "numCategories"), "field numCategories");
        check(hasField(c, "stateTable"), "field stateTable");
        check(hasField(c, "supplementaryCharCategoryTable"), "field supplementaryCharCategoryTable");
        check(hasField(c, "supportedVersion"), "field supportedVersion");
        check(hasField(c, "text"), "field text");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
