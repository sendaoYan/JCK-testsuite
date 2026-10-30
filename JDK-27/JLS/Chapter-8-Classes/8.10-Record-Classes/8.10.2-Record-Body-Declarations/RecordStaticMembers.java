/*
 * @test
 * @summary JLS 8.10.2: record classes may explicitly declare class methods, class
 *          variables, and nested types.
 * @run main RecordStaticMembers
 */

public record RecordStaticMembers(int x) {
    static int count = 0;
    static int add(int a, int b) { return a + b; }
    record Nested(int v) {}

    public static void main(String[] args) {
        RecordStaticMembers r = new RecordStaticMembers(5);
        count = 10;
        if (r.x() != 5) throw new AssertionError("component");
        if (count != 10) throw new AssertionError("class variable");
        if (add(2, 3) != 5) throw new AssertionError("class method");
        if (new Nested(7).v() != 7) throw new AssertionError("nested record");
        System.out.println("PASS");
    }
}
