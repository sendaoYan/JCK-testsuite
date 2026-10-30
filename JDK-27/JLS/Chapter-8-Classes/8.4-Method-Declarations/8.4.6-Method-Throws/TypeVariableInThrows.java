/*
 * @test
 * @summary JLS 8.4.6: a throws clause may name a type variable, provided its
 *          bound is a subtype of Throwable.
 * @run main TypeVariableInThrows
 */

public class TypeVariableInThrows {
    static <T extends Throwable> void failWith(Class<T> c) throws T {
        throw c.cast(new RuntimeException("boom"));
    }

    public static void main(String[] args) {
        try {
            failWith(RuntimeException.class);
            throw new AssertionError("expected RuntimeException");
        } catch (RuntimeException e) {
            if (!"boom".equals(e.getMessage())) throw new AssertionError("message: " + e.getMessage());
        }
        System.out.println("PASS");
    }
}
