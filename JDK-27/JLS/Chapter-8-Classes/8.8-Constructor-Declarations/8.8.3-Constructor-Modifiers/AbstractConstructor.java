/*
 * @test
 * @summary JLS 8.8.3: a constructor cannot be abstract.
 * @compile/fail AbstractConstructor.java
 */

class AbstractConstructor {
    abstract AbstractConstructor() {}   // error: modifier abstract not allowed here
}
