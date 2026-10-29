/*
 * @test
 * @summary JLS 3.10.6: line terminators in a text block are normalized to
 *          LF; CRLF and CR are translated to LF in step 1.
 * @run main TextBlockCRLFNormalization
 */
public class TextBlockCRLFNormalization {
    public static void main(String[] args) {
        String s = """
line1
line2
""";
        if (!"line1\nline2\n".equals(s)) {
            throw new AssertionError("CRLF not normalized to LF: " + s);
        }
        System.out.println("PASS");
    }
}
