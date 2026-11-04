/*
 * @test
 * @summary JLS 8.1 (Java SE 8): it is a compile-time error if a class has the same simple
 *          name as any of its enclosing classes or interfaces.
 * @compile/fail DuplicateEnclosingClassName.java
 */
class DuplicateEnclosingClassName {
    class Inner {
        class Inner { }   // error: class ... is already defined
    }
}
