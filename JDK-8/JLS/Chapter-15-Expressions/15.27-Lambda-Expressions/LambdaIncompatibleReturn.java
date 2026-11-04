/*
 * @test
 * @summary JLS 15.27.3 (Java SE 8): the result of a lambda body must be
 *          compatible with the return type of the target functional interface's
 *          method; otherwise it is a compile-time error.
 * @compile/fail LambdaIncompatibleReturn.java
 */

import java.util.function.Function;

class LambdaIncompatibleReturn {
    void m() {
        Function<String, Integer> f = s -> s;   // error: bad return type in lambda (String vs Integer)
    }
}
