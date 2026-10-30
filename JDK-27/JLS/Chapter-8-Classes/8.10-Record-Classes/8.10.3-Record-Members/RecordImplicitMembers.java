/*
 * @test
 * @summary JLS 8.10.3: a record class implicitly declares equals, hashCode, and
 *          toString derived from its record components.
 * @run main RecordImplicitMembers
 */

public record RecordImplicitMembers(int x, String s) {
    public static void main(String[] args) {
        RecordImplicitMembers a = new RecordImplicitMembers(1, "one");
        RecordImplicitMembers b = new RecordImplicitMembers(1, "one");
        RecordImplicitMembers c = new RecordImplicitMembers(2, "one");
        if (!a.equals(b)) throw new AssertionError("equals true");
        if (a.equals(c)) throw new AssertionError("equals false");
        if (a.hashCode() != b.hashCode()) throw new AssertionError("hashCode");
        if (!"RecordImplicitMembers[x=1, s=one]".equals(a.toString()))
            throw new AssertionError("toString: " + a.toString());
        System.out.println("PASS");
    }
}
