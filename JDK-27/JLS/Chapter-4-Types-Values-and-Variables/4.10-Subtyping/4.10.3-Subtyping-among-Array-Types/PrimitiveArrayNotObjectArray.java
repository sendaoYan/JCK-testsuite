/*
 * @test
 * @summary JLS 4.10.3: "Object >1 Object[]" only for reference array types; a
 *          primitive array type int[] is not a subtype of Object[]. Assigning
 *          an int[] to an Object[] must be a compile-time error.
 * @compile/fail PrimitiveArrayNotObjectArray.java
 */

class PrimitiveArrayNotObjectArray {
    void m() {
        int[] ia = new int[1];
        Object[] oa = ia;   // error: int[] is not Object[]
    }
}
