/*
 * @test
 * @summary JLS 9.1.4: a sealed interface may name its permitted direct
 *          subinterfaces and subclasses in a permits clause.
 * @run main SealedInterfacePermits
 */

sealed interface I permits C {}
final class C implements I {}

public class SealedInterfacePermits {
    public static void main(String[] args) {
        I i = new C();
        if (!(i instanceof I)) throw new AssertionError("permits");
        System.out.println("PASS");
    }
}
