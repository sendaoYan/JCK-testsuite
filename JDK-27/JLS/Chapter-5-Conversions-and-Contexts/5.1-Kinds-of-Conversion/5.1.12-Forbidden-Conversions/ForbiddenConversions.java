/*
 * @test
 * @summary JLS 5.1.12: any conversion that is not explicitly allowed is
 *          forbidden. No cast exists between boolean and any numeric type.
 * @compile/fail ForbiddenConversions.java
 */

class ForbiddenConversions {
    void m(boolean b) {
        int i = (int) b;      // error: cannot cast boolean to int
    }
}
