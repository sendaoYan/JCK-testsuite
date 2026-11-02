/*
 * @test
 * @summary org.w3c.dom.Text conformance: interface extends CharacterData.
 * @run main TextTest
 */

import org.w3c.dom.CharacterData;
import org.w3c.dom.Text;

public class TextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Text.class.isInterface(), "Text is an interface");
        check(CharacterData.class.isAssignableFrom(Text.class), "extends CharacterData");
        check(Text.class.getMethod("splitText", int.class) != null, "splitText(int)");
        check(Text.class.getMethod("isElementContentWhitespace") != null,
              "isElementContentWhitespace()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
