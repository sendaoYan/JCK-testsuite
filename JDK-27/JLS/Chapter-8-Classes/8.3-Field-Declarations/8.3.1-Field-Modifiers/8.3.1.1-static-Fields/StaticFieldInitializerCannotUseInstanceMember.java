/*
 * @test
 * @summary JLS 8.3.2: a class variable initializer may not refer by simple name
 *          to an instance variable.
 * @compile/fail StaticFieldInitializerCannotUseInstanceMember.java
 */
class StaticFieldInitializerCannotUseInstanceMember {
    int x;
    static int y = x;   // error: non-static variable x cannot be referenced from a static context
}
