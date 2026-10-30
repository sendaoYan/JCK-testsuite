/*
 * @test
 * @summary JLS 4.7 (Java SE 8): parameterized types are not reifiable, so they
 *          cannot be used as the type operand of instanceof.
 * @compile/fail NonReifiableInstanceof.java
 */
import java.util.*;

class NonReifiableInstanceof {
    void m(Object o) {
        boolean b = o instanceof List<String>;  // error: illegal generic type
    }
}
