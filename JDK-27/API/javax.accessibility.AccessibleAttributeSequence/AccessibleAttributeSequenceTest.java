/*
 * @test
 * @summary javax.accessibility.AccessibleAttributeSequence conformance: white-box structure + black-box behavior.
 * @run main AccessibleAttributeSequenceTest
 */

import java.lang.reflect.*;

public class AccessibleAttributeSequenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getFields()) if (f.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleAttributeSequence");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasField(c, "startIndex"), "public field startIndex");
        check(hasField(c, "endIndex"), "public field endIndex");
        check(hasField(c, "attributes"), "public field attributes");
    }

    static void testBlackBox() throws Exception {
        javax.swing.text.SimpleAttributeSet as = new javax.swing.text.SimpleAttributeSet();
        javax.accessibility.AccessibleAttributeSequence s =
                new javax.accessibility.AccessibleAttributeSequence(2, 7, as);
        check(s.startIndex == 2, "ctor startIndex");
        check(s.endIndex == 7, "ctor endIndex");
        check(s.attributes == as, "ctor attributes identity");

        s.startIndex = 3; s.endIndex = 9; s.attributes = null;
        check(s.startIndex == 3, "mutable startIndex");
        check(s.endIndex == 9, "mutable endIndex");
        check(s.attributes == null, "mutable attributes");

        javax.accessibility.AccessibleAttributeSequence empty =
                new javax.accessibility.AccessibleAttributeSequence(0, 0, null);
        check(empty.startIndex == 0 && empty.endIndex == 0 && empty.attributes == null,
                "zero/null ctor values retained");

        javax.accessibility.AccessibleAttributeSequence neg =
                new javax.accessibility.AccessibleAttributeSequence(-1, -2, as);
        check(neg.startIndex == -1 && neg.endIndex == -2, "negative indices retained");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
