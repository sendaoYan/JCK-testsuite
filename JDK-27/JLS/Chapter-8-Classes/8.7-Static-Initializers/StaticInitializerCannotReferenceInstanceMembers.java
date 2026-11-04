/*
 * @test
 * @summary JLS 8.7: a static initializer introduces a static context, so it cannot
 *          refer to an instance member of the class by simple name.
 * @compile/fail StaticInitializerCannotReferenceInstanceMembers.java
 */

class StaticInitializerCannotReferenceInstanceMembers {
    int x;
    static {
        x = 1;   // error: non-static variable x cannot be referenced from a static context
    }
}
