/*
 * @test
 * @summary JLS 4.5.2: "it is impossible to use a wildcard after the keyword new
 *          in a class instance creation expression." Creating an instance with
 *          a wildcard type argument must be a compile-time error.
 * @compile/fail WildcardAfterNew.java
 */

import java.util.ArrayList;
import java.util.List;

class WildcardAfterNew {
    void m() {
        List<?> x = new ArrayList<?>();   // error: wildcard not allowed after new
    }
}
