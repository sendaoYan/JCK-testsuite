/*
 * @test
 * @summary JLS 10.5 / 15.10.1: an array creation expression with a non-reifiable
 *          element type is a compile-time error, so the JVM can always perform
 *          the ArrayStoreException store check.
 * @compile/fail ArrayNonReifiableCreation.java
 */

import java.util.List;

class ArrayNonReifiableCreation {
    void m() {
        List<String>[] a = new List<String>[1];   // error: generic array creation
    }
}
