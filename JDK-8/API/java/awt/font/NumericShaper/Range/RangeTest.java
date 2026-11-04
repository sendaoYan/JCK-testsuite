/*
 * @test
 * @summary java.awt.font.NumericShaper.Range conformance: enum reflection.
 * @run main RangeTest
 */

public class RangeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.font.NumericShaper$Range");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "values"), "method values");
        check(hasMethod(c, "valueOf"), "method valueOf");
        Object[] consts = c.getEnumConstants();
        check(consts != null && consts.length == 35, "35 enum constants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object o : consts) names.add(o.toString());
        check(names.contains("EUROPEAN"), "constant EUROPEAN");
        check(names.contains("ARABIC"), "constant ARABIC");
        check(names.contains("EASTERN_ARABIC"), "constant EASTERN_ARABIC");
        check(names.contains("DEVANAGARI"), "constant DEVANAGARI");
        check(names.contains("BENGALI"), "constant BENGALI");
        check(names.contains("GURMUKHI"), "constant GURMUKHI");
        check(names.contains("GUJARATI"), "constant GUJARATI");
        check(names.contains("ORIYA"), "constant ORIYA");
        check(names.contains("TAMIL"), "constant TAMIL");
        check(names.contains("TELUGU"), "constant TELUGU");
        check(names.contains("KANNADA"), "constant KANNADA");
        check(names.contains("MALAYALAM"), "constant MALAYALAM");
        check(names.contains("THAI"), "constant THAI");
        check(names.contains("LAO"), "constant LAO");
        check(names.contains("TIBETAN"), "constant TIBETAN");
        check(names.contains("MYANMAR"), "constant MYANMAR");
        check(names.contains("ETHIOPIC"), "constant ETHIOPIC");
        check(names.contains("KHMER"), "constant KHMER");
        check(names.contains("MONGOLIAN"), "constant MONGOLIAN");
        check(names.contains("NKO"), "constant NKO");
        check(names.contains("MYANMAR_SHAN"), "constant MYANMAR_SHAN");
        check(names.contains("LIMBU"), "constant LIMBU");
        check(names.contains("NEW_TAI_LUE"), "constant NEW_TAI_LUE");
        check(names.contains("BALINESE"), "constant BALINESE");
        check(names.contains("SUNDANESE"), "constant SUNDANESE");
        check(names.contains("LEPCHA"), "constant LEPCHA");
        check(names.contains("OL_CHIKI"), "constant OL_CHIKI");
        check(names.contains("VAI"), "constant VAI");
        check(names.contains("SAURASHTRA"), "constant SAURASHTRA");
        check(names.contains("KAYAH_LI"), "constant KAYAH_LI");
        check(names.contains("CHAM"), "constant CHAM");
        check(names.contains("TAI_THAM_HORA"), "constant TAI_THAM_HORA");
        check(names.contains("TAI_THAM_THAM"), "constant TAI_THAM_THAM");
        check(names.contains("JAVANESE"), "constant JAVANESE");
        check(names.contains("MEETEI_MAYEK"), "constant MEETEI_MAYEK");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
