/*
 * @test
 * @summary JLS 5.6 (Java SE 27): in a numeric choice context, if a constant int
 *          result expression is not representable in the otherwise-byte result
 *          type, the promoted type becomes int, so assigning the switch
 *          expression to byte is a compile-time error.
 * @compile/fail NumericChoiceContextOverflow.java
 */

class NumericChoiceContextOverflow {
    void m() {
        // 128 is not representable in byte, so the promoted type is int.
        byte b = switch (0) { case 0 -> (byte) 1; default -> 128; };   // error: possible lossy conversion
    }
}
