/*
 * @test
 * @summary JLS 8.5.1 (Java SE 8): a static member class has no enclosing instance,
 *          so it cannot refer to an instance member of its enclosing class by
 *          simple name.
 * @compile/fail StaticMemberClassCannotAccessInstanceMembers.java
 */

class Outer {
    int x;
    static class Nested {
        int read() {
            return x;   // error: non-static variable x cannot be referenced from a static context
        }
    }
}
