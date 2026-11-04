/*
 * @test
 * @summary JLS 8.1.1 / 8.1.1.4: the static modifier pertains only to member
 *          classes, not to local classes; a static local class is a syntax error.
 * @compile/fail StaticLocalClassModifier.java
 */
class StaticLocalClassModifier {
    void m() {
        static class Local { }   // error: static modifier not allowed on local class
    }
}
