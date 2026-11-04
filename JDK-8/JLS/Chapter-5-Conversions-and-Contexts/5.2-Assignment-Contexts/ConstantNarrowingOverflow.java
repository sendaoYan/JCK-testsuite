/*
 * @test
 * @summary JLS 5.2 (Java SE 8): the implicit narrowing of a constant expression
 *          is allowed only if the constant value is representable in the target
 *          type. 128 is not representable in byte, so assigning int constant 128
 *          to byte is a compile-time error.
 * @compile/fail ConstantNarrowingOverflow.java
 */

class ConstantNarrowingOverflow {
    void m() {
        byte b = 128;   // error: incompatible types: possible lossy conversion from int to byte
    }
}
