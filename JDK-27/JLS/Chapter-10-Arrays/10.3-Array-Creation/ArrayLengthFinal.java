/*
 * @test
 * @summary JLS 10.3: an array's length is a final instance variable and cannot be
 *          assigned.
 * @compile/fail ArrayLengthFinal.java
 */

class ArrayLengthFinal {
    void m() {
        int[] a = new int[3];
        a.length = 5;   // error: cannot assign a value to final variable length
    }
}
