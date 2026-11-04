/*
 * @test
 * @summary java.awt.im.InputMethodHighlight conformance: class reflection.
 * @run main InputMethodHighlightTest
 */

public class InputMethodHighlightTest {
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
        Class<?> c = Class.forName("java.awt.im.InputMethodHighlight");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "isSelected"), "method isSelected");
        check(hasMethod(c, "getState"), "method getState");
        check(hasMethod(c, "getVariation"), "method getVariation");
        check(hasMethod(c, "getStyle"), "method getStyle");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // InputMethodHighlight constants
        check(java.awt.im.InputMethodHighlight.RAW_TEXT == 0, "RAW_TEXT == 0");
        check(java.awt.im.InputMethodHighlight.CONVERTED_TEXT == 1, "CONVERTED_TEXT == 1");
        java.awt.im.InputMethodHighlight u = java.awt.im.InputMethodHighlight.UNSELECTED_RAW_TEXT_HIGHLIGHT;
        check(!u.isSelected() && u.getState() == java.awt.im.InputMethodHighlight.RAW_TEXT,
              "UNSELECTED_RAW_TEXT_HIGHLIGHT state");
        java.awt.im.InputMethodHighlight sr = java.awt.im.InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
        check(sr.isSelected() && sr.getState() == java.awt.im.InputMethodHighlight.RAW_TEXT,
              "SELECTED_RAW_TEXT_HIGHLIGHT state");
        java.awt.im.InputMethodHighlight uc = java.awt.im.InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT;
        check(!uc.isSelected() && uc.getState() == java.awt.im.InputMethodHighlight.CONVERTED_TEXT,
              "UNSELECTED_CONVERTED_TEXT_HIGHLIGHT state");
        java.awt.im.InputMethodHighlight sc = java.awt.im.InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT;
        check(sc.isSelected() && sc.getState() == java.awt.im.InputMethodHighlight.CONVERTED_TEXT,
              "SELECTED_CONVERTED_TEXT_HIGHLIGHT state");
        // ctors
        java.awt.im.InputMethodHighlight h1 = new java.awt.im.InputMethodHighlight(true,
                java.awt.im.InputMethodHighlight.CONVERTED_TEXT);
        check(h1.isSelected() && h1.getState() == 1, "two-arg ctor");
        check(h1.getVariation() == 0, "default variation 0");
        check(h1.getStyle() == null, "default style null");
        java.awt.im.InputMethodHighlight h2 = new java.awt.im.InputMethodHighlight(false,
                java.awt.im.InputMethodHighlight.RAW_TEXT, 7);
        check(!h2.isSelected() && h2.getVariation() == 7, "three-arg ctor variation");
        java.util.Map<java.awt.font.TextAttribute, Object> style =
                new java.util.HashMap<java.awt.font.TextAttribute, Object>();
        style.put(java.awt.font.TextAttribute.FOREGROUND, java.awt.Color.red);
        java.awt.im.InputMethodHighlight h3 = new java.awt.im.InputMethodHighlight(true,
                java.awt.im.InputMethodHighlight.CONVERTED_TEXT, 3, style);
        check(h3.getStyle() == style, "four-arg ctor style identity");
        check(h3.getVariation() == 3, "four-arg ctor variation");
        // no equals override: identity semantics (locked observed)
        check(!h1.equals(new java.awt.im.InputMethodHighlight(true,
                java.awt.im.InputMethodHighlight.CONVERTED_TEXT)),
              "InputMethodHighlight has no equals override (identity)");
        check(!h1.equals(null), "equals(null) false");
        // constants and instances are usable as hashtable keys by identity
        java.util.Set<java.awt.im.InputMethodHighlight> set =
                new java.util.HashSet<java.awt.im.InputMethodHighlight>();
        check(set.add(h1) && set.add(h2) && !set.add(h1), "identity set semantics");
        // InputSubset extends Character.Subset with equals/hashCode from Subset
        check(java.awt.im.InputSubset.LATIN instanceof Character.Subset, "LATIN is Character.Subset");
        check(java.awt.im.InputSubset.LATIN.equals(java.awt.im.InputSubset.LATIN), "LATIN equals self");
        check(!java.awt.im.InputSubset.LATIN.equals(java.awt.im.InputSubset.LATIN_DIGITS), "distinct subsets");
        check(java.awt.im.InputSubset.LATIN.hashCode() == java.awt.im.InputSubset.LATIN.hashCode(), "Subset hashCode stable");
        check(java.awt.im.InputSubset.LATIN.toString().equals("LATIN"), "LATIN toString is its name");
        // all documented constants present and distinct
        java.awt.im.InputSubset[] subs = {java.awt.im.InputSubset.LATIN, java.awt.im.InputSubset.LATIN_DIGITS,
                java.awt.im.InputSubset.TRADITIONAL_HANZI, java.awt.im.InputSubset.SIMPLIFIED_HANZI,
                java.awt.im.InputSubset.KANJI, java.awt.im.InputSubset.HANJA,
                java.awt.im.InputSubset.HALFWIDTH_KATAKANA, java.awt.im.InputSubset.FULLWIDTH_LATIN,
                java.awt.im.InputSubset.FULLWIDTH_DIGITS};
        String[] names = {"LATIN","LATIN_DIGITS","TRADITIONAL_HANZI","SIMPLIFIED_HANZI","KANJI","HANJA",
                "HALFWIDTH_KATAKANA","FULLWIDTH_LATIN","FULLWIDTH_DIGITS"};
        java.util.Set<String> names_ = new java.util.HashSet<String>();
        for (int i = 0; i < subs.length; i++) {
            check(names_.add(subs[i].toString()), "constant distinct: " + names[i]);
        }
        // Character.Subset equality across different instances of same name would be equal,
        // but InputSubset instances are singletons: same instance each access
        check(java.awt.im.InputSubset.HANJA == java.awt.im.InputSubset.HANJA, "singleton identity");
    }

}
