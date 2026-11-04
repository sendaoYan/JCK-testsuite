/*
 * @test
 * @summary JLS 8.1.3 (Java SE 8): it is a compile-time error if an inner class
 *          declares a member that is explicitly or implicitly static, unless the
 *          member is a constant variable.
 * @compile/fail InnerClassStaticMemberNotConstantVariable.java
 */
class Outer {
    class Inner {
        static int y = 4;   // error: inner classes cannot have static declarations
    }
}
