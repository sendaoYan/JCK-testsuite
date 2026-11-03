/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedText conformance: white-box structure + black-box behavior.
 * @run main AccessibleExtendedTextTest
 */

import java.lang.reflect.*;
import java.awt.*;

public class AccessibleExtendedTextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class Model implements javax.accessibility.AccessibleExtendedText {
        String text = "hello world";
        public String getTextRange(int start, int end) { return text.substring(start, end); }
        public javax.accessibility.AccessibleTextSequence getTextSequenceAt(int start, int end) {
            return new javax.accessibility.AccessibleTextSequence(start, end, text.substring(start, end));
        }
        public javax.accessibility.AccessibleTextSequence getTextSequenceAfter(int part, int index) {
            return new javax.accessibility.AccessibleTextSequence(index, index + 1, "a");
        }
        public javax.accessibility.AccessibleTextSequence getTextSequenceBefore(int part, int index) {
            return new javax.accessibility.AccessibleTextSequence(index - 1, index, "b");
        }
        public Rectangle getTextBounds(int start, int end) { return new Rectangle(start, 0, end - start, 1); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedText");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getTextRange"), "declares getTextRange");
        check(hasMethod(c, "getTextSequenceAt"), "declares getTextSequenceAt");
        check(hasMethod(c, "getTextSequenceAfter"), "declares getTextSequenceAfter");
        check(hasMethod(c, "getTextSequenceBefore"), "declares getTextSequenceBefore");
        check(hasMethod(c, "getTextBounds"), "declares getTextBounds");
    }

    static void testBlackBox() throws Exception {
        check(javax.accessibility.AccessibleExtendedText.LINE == 4, "LINE == 4");
        check(javax.accessibility.AccessibleExtendedText.ATTRIBUTE_RUN == 5, "ATTRIBUTE_RUN == 5");

        Model m = new Model();
        check("hello".equals(m.getTextRange(0, 5)), "getTextRange");
        javax.accessibility.AccessibleTextSequence seq = m.getTextSequenceAt(0, 5);
        check(seq.startIndex == 0 && seq.endIndex == 5 && "hello".equals(seq.text), "getTextSequenceAt");
        check("a".equals(m.getTextSequenceAfter(javax.accessibility.AccessibleExtendedText.LINE, 0).text),
                "getTextSequenceAfter");
        check("b".equals(m.getTextSequenceBefore(javax.accessibility.AccessibleExtendedText.LINE, 1).text),
                "getTextSequenceBefore");
        Rectangle r = m.getTextBounds(0, 5);
        check(r.x == 0 && r.width == 5, "getTextBounds");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
