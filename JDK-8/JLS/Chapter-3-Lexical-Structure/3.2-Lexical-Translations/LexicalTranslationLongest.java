/*
 * @test
 * @summary JLS 3.2 (Java SE 8): the longest possible translation is used at each
 *          step, so a--b is tokenized as a, --, b (not a, -, -, b), which is
 *          not a grammatically correct expression.
 * @compile/fail LexicalTranslationLongest.java
 */

public class LexicalTranslationLongest {
    public static void main(String[] args) {
        int a = 5;
        int b = 3;
        int c = a--b;   // tokenized as a, --, b: not a valid expression
    }
}
