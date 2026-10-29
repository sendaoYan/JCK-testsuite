/*
 * @test
 * @summary JLS 6.5.6.2 Example 6.5.6.2-2: a class variable cannot be accessed
 *          through a parameterized type; Foo<String>.classVar is illegal.
 * @compile/fail ParameterizedTypeStaticField.java
 */

class Foo<T> {
    public static int classVar = 42;
}

class ParameterizedTypeStaticField {
    void m() {
        Foo<String>.classVar = 91;   // error: parameterized type qualifier
    }
}
