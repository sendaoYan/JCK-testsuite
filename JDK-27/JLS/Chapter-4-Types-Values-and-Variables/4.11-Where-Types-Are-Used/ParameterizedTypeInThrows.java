/*
 * @test
 * @summary JLS 4.11: types that are exceptions are non-generic; a type that is
 *          a parameterized reference type may not be used in a throws clause.
 * @compile/fail ParameterizedTypeInThrows.java
 */

import java.util.List;
import java.util.ArrayList;

class ParameterizedTypeInThrows {
    // List<RuntimeException> is a parameterized reference type; it may not be
    // thrown because exception types are non-generic.
    void m() throws List<RuntimeException> {
        throw new RuntimeException();
    }
}
