/*
 * @test
 * @summary JLS 15.10.1 (Java SE 8): "It is a compile-time error if the
 *          ClassOrInterfaceType is not a reifiable type."  Creating an array of
 *          a parameterized type is forbidden.
 * @compile/fail NonReifiableArrayCreation.java
 */

import java.util.List;

class NonReifiableArrayCreation {
    List<String>[] m() {
        return new List<String>[10];   // error: generic array creation
    }
}
