/*
 * @test
 * @summary JLS 8.4.4: a generic method's type arguments are often inferred at
 *          invocation and the method operates across the inferred type.
 * @run main GenericMethodInference
 */
public class GenericMethodInference {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static <T> T identity(T x) { return x; }
    static <T> String typeName(T x) { return x.getClass().getName(); }
    public static void main(String[] args) {
        check(identity("s").equals("s"), "generic method identity with inferred String");
        check(identity(42) == 42, "generic method identity with boxed int");
        check(typeName("x").equals("java.lang.String"), "type argument inferred");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
