/*
 * @test
 * @summary JLS 8.4.3.2: unqualified references to instance variables from a
 *          static context are disallowed.
 * @compile/fail StaticMethodAccessInstanceField.java
 */

class StaticMethodAccessInstanceField {
    int x;
    static void m() {
        x = 5;   // error: non-static variable x cannot be referenced
    }
}
