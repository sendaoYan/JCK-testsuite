/*
 * @test
 * @summary JLS 8.8.3: a constructor cannot be final.
 * @compile/fail FinalConstructor.java
 */

class FinalConstructor {
    final FinalConstructor() {}   // error: modifier final not allowed here
}
