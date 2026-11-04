/*
 * @test
 * @summary JLS 4.11 (Java SE 8): exception types are non-generic; a type that is
 *          a parameterized reference type may not be used in a throws clause.
 * @compile/fail ParameterizedTypeInThrows.java
 */

import java.util.List;

class ParameterizedTypeInThrows {
    // List<RuntimeException> is a parameterized reference type, not a subtype
    // of Throwable; it may not be used in a throws clause.
    void m() throws List<RuntimeException> {   // error: generic class may not extend java.lang.Throwable
        throw new RuntimeException();
    }
}
