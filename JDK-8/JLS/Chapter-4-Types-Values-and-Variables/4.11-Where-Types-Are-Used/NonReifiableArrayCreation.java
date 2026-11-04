/*
 * @test
 * @summary JLS 4.11 (Java SE 8): a type is used in a class instance creation or
 *          array creation expression only if it is reifiable. Creating an array
 *          whose component type is a parameterized type is a compile-time error
 *          (generic array creation).
 * @compile/fail NonReifiableArrayCreation.java
 */

import java.util.List;

class NonReifiableArrayCreation {
    void m() {
        List<String>[] a = new List<String>[1];   // error: generic array creation
    }
}
