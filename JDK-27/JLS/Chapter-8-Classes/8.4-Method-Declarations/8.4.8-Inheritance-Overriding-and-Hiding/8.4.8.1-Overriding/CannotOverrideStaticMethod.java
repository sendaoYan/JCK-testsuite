/*
 * @test
 * @summary JLS 8.4.8.1: an instance method cannot override a static method.
 * @compile/fail CannotOverrideStaticMethod.java
 */

class Super {
    static void m() {}
}

class CannotOverrideStaticMethod extends Super {
    void m() {}   // error: cannot override static method
}
