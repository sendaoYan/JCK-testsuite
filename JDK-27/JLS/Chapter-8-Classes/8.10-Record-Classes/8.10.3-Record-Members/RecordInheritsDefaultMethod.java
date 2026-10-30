/*
 * @test
 * @summary JLS 8.10.3: a record class can inherit default methods from its direct
 *          superinterfaces.
 * @run main RecordInheritsDefaultMethod
 */

public class RecordInheritsDefaultMethod {
    interface Logging {
        default String logAction() { return "Logged"; }
    }
    record Point(int i, int j) implements Logging {}

    public static void main(String[] args) {
        Point p = new Point(10, 20);
        if (!"Logged".equals(p.logAction())) throw new AssertionError("default method");
        System.out.println("PASS");
    }
}
