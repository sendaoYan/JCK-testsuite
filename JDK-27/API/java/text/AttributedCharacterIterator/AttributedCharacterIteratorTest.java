/*
 * @test
 * @summary java.text.AttributedCharacterIterator conformance: white-box structure +
 *          black-box behavior.
 * @run main AttributedCharacterIteratorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AttributedCharacterIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final AttributedCharacterIterator.Attribute LANG =
            AttributedCharacterIterator.Attribute.LANGUAGE;
    static final AttributedCharacterIterator.Attribute READ =
            AttributedCharacterIterator.Attribute.READING;

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.AttributedCharacterIterator");
        check(c.isInterface(), "is an interface");
        check(CharacterIterator.class.isAssignableFrom(c),
                "extends CharacterIterator");
        for (String m : new String[] {
                "getRunStart", "getRunLimit", "getRunStart", "getRunLimit"})
            check(c.getDeclaredMethod(m) != null, "declares " + m);
        check(c.getDeclaredMethod("getRunStart",
                AttributedCharacterIterator.Attribute.class) != null,
                "getRunStart(Attribute)");
        check(c.getDeclaredMethod("getRunLimit",
                AttributedCharacterIterator.Attribute.class) != null,
                "getRunLimit(Attribute)");
        Method rs = c.getDeclaredMethod("getRunStart", Set.class);
        check(Modifier.isAbstract(rs.getModifiers()), "getRunStart(Set) abstract");
        check(c.getDeclaredMethod("getAttributes").getReturnType() == Map.class,
                "getAttributes() -> Map");
        check(c.getDeclaredMethod("getAttribute",
                AttributedCharacterIterator.Attribute.class).getReturnType() == Object.class,
                "getAttribute(Attribute) -> Object");
        check(c.getDeclaredMethod("getAllAttributeKeys").getReturnType() == Set.class,
                "getAllAttributeKeys() -> Set");
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods())
            if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == c.getDeclaredMethods().length,
                "all declared methods abstract (pure contract, " + abstractCount + ")");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // text: 0123456789, LANGUAGE: [0,4)="en" [4,10)="fr"; READING: [2,8)="r1"
        AttributedString as = new AttributedString("0123456789");
        as.addAttribute(LANG, "en", 0, 4);
        as.addAttribute(LANG, "fr", 4, 10);
        as.addAttribute(READ, "r1", 2, 8);
        AttributedCharacterIterator it = as.getIterator();

        // 1. inherited CharacterIterator navigation reconstructs the text
        StringBuilder sb = new StringBuilder();
        for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next())
            sb.append(ch);
        check("0123456789".contentEquals(sb), "first/next traversal rebuilds text");
        check(it.getBeginIndex() == 0 && it.getEndIndex() == 10, "begin/end of whole text");

        // 2. run boundaries driven by all attributes together
        int[][] runs = {{0, 2}, {2, 4}, {4, 8}, {8, 10}};
        int i = 0;
        for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next(), i++) {
            int idx = it.getIndex();
            int[] want = runs[bucket(idx)];
            check(it.getRunStart() == want[0] && it.getRunLimit() == want[1],
                    "run of all attributes at " + idx);
        }
        check(i == 10, "iterated every index");

        // 3. per-attribute run boundaries (at positions where the attribute is present)
        it.setIndex(0);
        check(it.getRunStart(LANG) == 0 && it.getRunLimit(LANG) == 4, "LANG run at index 0");
        it.setIndex(6);
        check(it.getRunStart(LANG) == 4 && it.getRunLimit(LANG) == 10, "LANG run at index 6");
        check(it.getRunStart(READ) == 2 && it.getRunLimit(READ) == 8, "READ run at index 6");
        it.setIndex(3);
        check(it.getRunStart(READ) == 2 && it.getRunLimit(READ) == 8, "READ run at index 3");
        check(it.getRunStart(LANG) < it.getRunStart(),
                "single-attr run can start before combined run");

        // 4. Set-of-attributes runs: smallest span where all listed attributes are stable
        Set<AttributedCharacterIterator.Attribute> both = new HashSet<>();
        both.add(LANG); both.add(READ);
        it.setIndex(0);
        check(it.getRunStart(both) == 0, "getRunStart(Set) at 0");
        check(it.getRunLimit(both) == 2, "getRunLimit(Set) at 0 ends where READ appears");
        it.setIndex(6);
        check(it.getRunStart(both) == 4 && it.getRunLimit(both) == 8,
                "getRunStart/Limit(Set) at 6 covers [4,8)");
        Set<AttributedCharacterIterator.Attribute> readOnly = Collections.singleton(READ);
        check(it.getRunStart(readOnly) == 2 && it.getRunLimit(readOnly) == 8,
                "getRunStart/Limit(Set of READ only)");

        // 5. getAttributes / getAttribute values at a position
        it.setIndex(1);
        Map<AttributedCharacterIterator.Attribute, Object> attrs = it.getAttributes();
        check(attrs.size() == 1 && "en".equals(attrs.get(LANG)),
                "getAttributes at 1: only LANGUAGE=en");
        check("en".equals(it.getAttribute(LANG)) && it.getAttribute(READ) == null,
                "getAttribute present/absent at 1");
        it.setIndex(3);
        check("en".equals(it.getAttribute(LANG)) && "r1".equals(it.getAttribute(READ)),
                "both attributes present at 3");
        it.setIndex(9);
        check("fr".equals(it.getAttribute(LANG)) && it.getAttribute(READ) == null,
                "only LANG=fr at 9");
        it.last();
        Map<AttributedCharacterIterator.Attribute, Object> atLast = it.getAttributes();
        check("fr".equals(atLast.get(LANG)), "getAttributes at last char");

        // 6. getAllAttributeKeys
        Set<AttributedCharacterIterator.Attribute> keys = it.getAllAttributeKeys();
        check(keys.size() == 2 && keys.contains(LANG) && keys.contains(READ),
                "getAllAttributeKeys lists both attributes");
        check(it.getAllAttributeKeys().size() == 2, "getAllAttributeKeys stable across calls");

        // 7. unattributed text: no keys, full-width run
        AttributedCharacterIterator plain =
                new AttributedString("plain").getIterator();
        check(plain.getAllAttributeKeys().isEmpty(), "no attributes -> no keys");
        check(plain.getAttributes().isEmpty(), "no attributes -> empty map");
        check(plain.getAttribute(LANG) == null, "getAttribute null on plain text");
        check(plain.getRunStart() == 0 && plain.getRunLimit() == 5, "single run over plain text");
        check(plain.getRunStart(LANG) == 0, "absent-attr run start clamps to begin");

        // 8. clone through the interface reference
        AttributedCharacterIterator src8 = new AttributedString("abc").getIterator();
        AttributedCharacterIterator cl = (AttributedCharacterIterator) src8.clone();
        check(cl != src8, "clone distinct from source");
        check(cl.current() == 'a' && cl.first() == 'a', "clone navigates like the source");
        check(cl.getBeginIndex() == 0 && cl.getEndIndex() == 3, "clone preserves bounds");
    }

    static int bucket(int idx) {
        if (idx < 2) return 0;
        if (idx < 4) return 1;
        if (idx < 8) return 2;
        return 3;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
