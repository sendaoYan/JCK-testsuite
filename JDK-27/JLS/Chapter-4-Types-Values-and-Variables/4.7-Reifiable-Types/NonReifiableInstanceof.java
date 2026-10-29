/*
 * @test
 * @summary JLS 4.7: a parameterized type that is not reifiable (a concrete
 *          type argument) may not follow the instanceof operator. This is a
 *          compile-time error.
 * @compile/fail NonReifiableInstanceof.java
 */

import java.util.List;
import java.util.ArrayList;

class NonReifiableInstanceof {
    boolean m(Object o) {
        return o instanceof List<String>;   // error: List<String> is not reifiable
    }
}
