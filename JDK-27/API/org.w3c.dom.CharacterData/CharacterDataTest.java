/*
 * @test
 * @summary org.w3c.dom.CharacterData conformance: interface extends Node.
 * @run main CharacterDataTest
 */

import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;

public class CharacterDataTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(CharacterData.class.isInterface(), "CharacterData is an interface");
        check(Node.class.isAssignableFrom(CharacterData.class), "extends Node");
        check(CharacterData.class.getMethod("getData") != null, "getData()");
        check(CharacterData.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
