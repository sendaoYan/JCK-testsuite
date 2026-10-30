/*
 * @test
 * @summary JLS 8.3.1.4: it is a compile-time error if a final variable is also
 *          declared volatile.
 * @compile/fail FinalVolatileField.java
 */

class FinalVolatileField {
    final volatile int x = 1;   // error: illegal combination of modifiers
}
