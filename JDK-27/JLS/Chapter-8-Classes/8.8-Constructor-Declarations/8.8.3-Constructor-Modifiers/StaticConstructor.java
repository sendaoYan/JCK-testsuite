/*
 * @test
 * @summary JLS 8.8.3: a constructor cannot be static.
 * @compile/fail StaticConstructor.java
 */

class StaticConstructor {
    static StaticConstructor() {}   // error: modifier static not allowed here
}
