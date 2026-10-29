/*
 * @test
 * @summary JLS 6.1: a declaration may use the keyword _ (underscore) in place of
 *          an identifier to declare an unnamed local variable, unnamed exception
 *          parameter, or unnamed lambda parameter. Such an entity cannot be
 *          referred to by name, so the underscore may be reused in one scope.
 * @run main UnnamedVariables
 */

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class UnnamedVariables {
    public static void main(String[] args) {
        // Unnamed local variables may be declared more than once in one scope.
        int _ = 1;
        int _ = 2;

        // Unnamed lambda parameter (single).
        Consumer<Integer> c = _ -> { };
        c.accept(3);

        // Unnamed lambda parameters (two).
        BiConsumer<Integer, Integer> bc = (_, _) -> { };
        bc.accept(1, 2);

        // Unnamed exception parameter.
        try {
            throw new IllegalStateException("boom");
        } catch (IllegalStateException _) {
            // the exception object is not referred to by name
        }

        System.out.println("PASS");
    }
}
