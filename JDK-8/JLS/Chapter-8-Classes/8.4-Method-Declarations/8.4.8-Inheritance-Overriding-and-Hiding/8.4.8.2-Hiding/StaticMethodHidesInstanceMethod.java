/*
 * @test
 * @summary JLS 8.4.8.2 (Java SE 8): it is a compile-time error if a static method hides an
 *          instance method.
 * @compile/fail StaticMethodHidesInstanceMethod.java
 */
class A { void m() { } }
class StaticMethodHidesInstanceMethod extends A {
    static void m() { }   // error: m() in ... cannot override m() in A (overriding method is static)
}
