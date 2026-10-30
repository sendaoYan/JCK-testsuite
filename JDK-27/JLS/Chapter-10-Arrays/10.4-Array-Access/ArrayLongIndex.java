/*
 * @test
 * @summary JLS 10.4: arrays must be indexed by int values; using a long index is
 *          a compile-time error.
 * @compile/fail ArrayLongIndex.java
 */

class ArrayLongIndex {
    void m(int[] a) {
        a[0L] = 1;   // error: long index not permitted
    }
}
