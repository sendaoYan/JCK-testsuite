/*
 * @test
 * @summary JLS 3.10.6 (Example 3.10.6-2, Story2): placing the closing
 *          delimiter on the last content line makes the first three
 *          consecutive double quotes the closing delimiter, leaving a
 *          stray double quote -> compile-time error.
 * @compile/fail TextBlockStrayDelimiter.java
 */
public class TextBlockStrayDelimiter {
    public static void main(String[] args) {
        String story = """
            "The question is," said Humpty Dumpty,
            "which is to be master - that's all."""";
    }
}
