/*
 * @test
 * @summary JLS 4.11: the element type in an array creation expression must be a
 *          reifiable type; a parameterized type with a concrete argument is not
 *          reifiable and cannot be used as the element type of a new array.
 * @compile/fail NonReifiableArrayCreation.java
 */

import java.util.List;

class NonReifiableArrayCreation {
    void m() {
        List<String>[] arr = new List<String>[1];   // error: generic array creation
    }
}
