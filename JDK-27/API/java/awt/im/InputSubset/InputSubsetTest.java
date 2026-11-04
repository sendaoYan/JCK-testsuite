/*
 * @test
 * @summary java.awt.im.InputSubset conformance: reflected type shape.
 * @run main InputSubsetTest
 */

import java.lang.reflect.*;

public class InputSubsetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.im.InputSubset", false, InputSubsetTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.im.InputSubset");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(),
              "public/protected/member");
        if (c.isEnum()) {
            check(c.getEnumConstants() != null, "enum has constants");
        } else {
            check(c.getEnumConstants() == null, "non-enum returns null constants");
        }
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) {
            check(Modifier.isInterface(m), "interface modifier consistent");
        } else if (c != Object.class) {
            check(c.getSuperclass() != null, "class superclass non-null");
        }
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
