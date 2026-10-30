/*
 * @test
 * @summary JVMS 6.5: invokevirtual and invokeinterface perform dynamic method
 *          dispatch based on the runtime class of the receiver.
 * @compile Animal.java
 * @compile MethodInvoke.jasm
 * @run main MethodInvokeTest
 */

public class MethodInvokeTest {
    public static void main(String[] args) {
        Animal animal = new Animal();
        Animal dog = new Dog();
        Speaker speaker = new Dog();

        if (!MethodInvoke.callName(animal).equals("animal"))
            throw new AssertionError("invokevirtual base dispatch");
        if (!MethodInvoke.callName(dog).equals("dog"))
            throw new AssertionError("invokevirtual override dispatch");
        if (!MethodInvoke.callSpeak(speaker).equals("woof"))
            throw new AssertionError("invokeinterface dispatch");

        System.out.println("PASS");
    }
}
