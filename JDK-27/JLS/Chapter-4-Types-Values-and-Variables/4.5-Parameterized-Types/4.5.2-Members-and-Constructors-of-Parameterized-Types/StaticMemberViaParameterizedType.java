/*
 * @test
 * @summary JLS 4.5.2: "A static member that is declared in a generic class or
 *          interface must be referred to using the name of the generic class
 *          or interface, or a compile-time error occurs." Referring to a
 *          static member through a parameterized type is a compile-time error.
 * @compile/fail StaticMemberViaParameterizedType.java
 */

class Generic<T> {
    static int count;
}

class StaticMemberViaParameterizedType {
    void m() {
        Generic<String>.count = 1;   // error: static member via parameterized type
    }
}
