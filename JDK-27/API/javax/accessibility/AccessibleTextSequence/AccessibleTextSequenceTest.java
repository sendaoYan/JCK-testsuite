/*
 * @test
 * @summary javax.accessibility.AccessibleTextSequence conformance: white-box structure + black-box behavior.
 * @run main AccessibleTextSequenceTest
 */

import java.lang.reflect.*;

public class AccessibleTextSequenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getFields()) if (f.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleTextSequence");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasField(c, "startIndex"), "public field startIndex");
        check(hasField(c, "endIndex"), "public field endIndex");
        check(hasField(c, "text"), "public field text");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleTextSequence s =
                new javax.accessibility.AccessibleTextSequence(0, 5, "hello");
        check(s.startIndex == 0, "ctor startIndex");
        check(s.endIndex == 5, "ctor endIndex");
        check("hello".equals(s.text), "ctor text");

        s.text = "bye";
        check("bye".equals(s.text), "mutable text");
        s.startIndex = 10; s.endIndex = 20;
        check(s.startIndex == 10 && s.endIndex == 20, "mutable indices");

        javax.accessibility.AccessibleTextSequence nul =
                new javax.accessibility.AccessibleTextSequence(-1, -1, null);
        check(nul.startIndex == -1 && nul.endIndex == -1 && nul.text == null,
                "negative/null ctor values retained");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
