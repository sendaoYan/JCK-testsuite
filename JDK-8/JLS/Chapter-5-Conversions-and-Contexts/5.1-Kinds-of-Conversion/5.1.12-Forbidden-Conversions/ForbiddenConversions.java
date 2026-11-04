/*
 * @test
 * @summary JLS 5.1.12 (Java SE 8): "Any conversion that is not explicitly
 *          allowed is forbidden." No conversion exists between boolean and any
 *          non-boolean type, so an assignment of int to boolean is a
 *          compile-time error.
 * @compile/fail ForbiddenConversions.java
 */

class ForbiddenConversions {
    void m() {
        boolean b = 1;   // error: incompatible types: int cannot be converted to boolean
    }
}
