/*
 * @test
 * @summary JLS 4.10.3 (Java SE 8): a primitive array type int[] is not a subtype
 *          of Object[]. Assigning an int[] to an Object[] is a compile-time
 *          error (incompatible types).
 * @compile/fail PrimitiveArrayNotObjectArray.java
 */

class PrimitiveArrayNotObjectArray {
    void m() {
        int[] ia = new int[1];
        Object[] oa = ia;   // error: incompatible types: int[] cannot be converted to Object[]
    }
}
