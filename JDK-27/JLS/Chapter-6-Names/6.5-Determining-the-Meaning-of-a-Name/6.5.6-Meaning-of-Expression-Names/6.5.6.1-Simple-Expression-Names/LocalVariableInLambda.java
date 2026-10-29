/*
 * @test
 * @summary JLS 6.5.6.1 Example 6.5.6.1-3: a local variable that is neither final
 *          nor effectively final cannot be referenced from a lambda expression.
 * @compile/fail LocalVariableInLambda.java
 */

class LocalVariableInLambda {
    void m() {
        for (int i = 0; i < 3; i++) {
            Runnable r = () -> System.out.println(i);   // error: i not effectively final
        }
    }
}
