/*
 * @test
 * @summary JLS 5.7 (Java SE 27): unlike casting contexts, testing contexts do
 *          not permit narrowing reference conversions that are unchecked. An
 *          instanceof pattern whose type requires an unchecked narrowing
 *          reference conversion (Object -> List<String>) is a compile-time error.
 * @compile/fail UncheckedNarrowingForbidden.java
 */

import java.util.List;

class UncheckedNarrowingForbidden {
    void m(Object o) {
        // Object -> List<String> is an unchecked narrowing reference conversion,
        // which a testing context does not allow.
        if (o instanceof List<String> l) {   // error: Object cannot be safely cast to List<String>
        }
    }
}
