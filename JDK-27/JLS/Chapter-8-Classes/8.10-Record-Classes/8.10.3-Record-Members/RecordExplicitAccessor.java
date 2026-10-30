/*
 * @test
 * @summary JLS 8.10.3: an accessor method may be declared explicitly, provided it
 *          is a public instance method with the component's type and no formal
 *          parameters or throws clause.
 * @run main RecordExplicitAccessor
 */

public record RecordExplicitAccessor(int x) {
    public int x() { return this.x < 100 ? this.x : 100; }   // explicit accessor

    public static void main(String[] args) {
        RecordExplicitAccessor r = new RecordExplicitAccessor(200);
        if (r.x() != 100) throw new AssertionError("clipped accessor: " + r.x());
        System.out.println("PASS");
    }
}
