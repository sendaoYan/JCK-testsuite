/*
 * @test
 * @summary JLS 3.10.6: a text block whose closing delimiter """ is missing is a
 *          compile-time error (unclosed text block).
 * @compile/fail TextBlockUnclosed.java
 */
public class TextBlockUnclosed {
    public static void main(String[] args) {
        String s = """
abc
    }
}
