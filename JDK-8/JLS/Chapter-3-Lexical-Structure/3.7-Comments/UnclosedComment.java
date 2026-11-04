/*
 * @test
 * @summary JLS 3.7 (Java SE 8): a traditional comment must be closed before the
 *          end of the file; an unterminated comment is a compile-time error.
 * @compile/fail UnclosedComment.java
 */
public class UnclosedComment {
    public static void main(String[] args) {
        int x = 1; /* unterminated comment
    }
}
