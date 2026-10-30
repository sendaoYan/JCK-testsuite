/*
 * @test
 * @summary JLS 9.3.1 (Example 9.3.1-1): an interface field initializer may not
 *          use the simple name of a field declared to its right, nor the field
 *          itself.
 * @compile/fail InterfaceFieldForwardReference.java
 */

interface InterfaceFieldForwardReference {
    float f = j;     // error: forward reference to j
    int j = 1;
    int k = k + 1;   // error: self reference to k
}
