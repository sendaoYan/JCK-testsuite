/*
 * @test
 * @summary java.lang.LayerInstantiationException conformance: exception hierarchy and constructors.
 * @run main LayerInstantiationExceptionTest
 */
public class LayerInstantiationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(java.lang.RuntimeException.class.isAssignableFrom(LayerInstantiationException.class), "extends RuntimeException");
        LayerInstantiationException e = new LayerInstantiationException("msg"); check("msg".equals(e.getMessage()), "message");
        LayerInstantiationException e0 = new LayerInstantiationException(); check(e0.getMessage() == null, "no-arg");
        Throwable c = new IllegalStateException("c"); check(new LayerInstantiationException(c).getCause() == c, "throwable ctor");
        check(new LayerInstantiationException("m", c).getCause() == c, "message+cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
