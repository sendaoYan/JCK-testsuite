/*
 * @test
 * @summary org.w3c.dom.Comment conformance: interface extends CharacterData.
 * @run main CommentTest
 */

import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;

public class CommentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Comment.class.isInterface(), "Comment is an interface");
        check(CharacterData.class.isAssignableFrom(Comment.class),
              "extends CharacterData");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
