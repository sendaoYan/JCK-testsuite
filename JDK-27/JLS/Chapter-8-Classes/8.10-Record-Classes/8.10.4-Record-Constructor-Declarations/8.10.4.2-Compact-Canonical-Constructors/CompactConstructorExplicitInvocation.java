/*
 * @test
 * @summary JLS 8.10.4.2: the body of a compact constructor must not contain an
 *          explicit constructor invocation.
 * @compile/fail CompactConstructorExplicitInvocation.java
 */

record Point(int x) {
    Point {
        this(0);   // error: constructor invocation not allowed in compact constructor
    }
}
