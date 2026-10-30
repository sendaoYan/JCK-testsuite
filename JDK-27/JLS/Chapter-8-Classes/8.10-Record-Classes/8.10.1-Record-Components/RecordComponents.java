/*
 * @test
 * @summary JLS 8.10.1: each record component corresponds to a private final field
 *          and a public accessor method.
 * @run main RecordComponents
 */

public record RecordComponents(int x, String s) {
    public static void main(String[] args) {
        RecordComponents r = new RecordComponents(10, "hi");
        if (r.x() != 10) throw new AssertionError("x");
        if (!"hi".equals(r.s())) throw new AssertionError("s");
        System.out.println("PASS");
    }
}
