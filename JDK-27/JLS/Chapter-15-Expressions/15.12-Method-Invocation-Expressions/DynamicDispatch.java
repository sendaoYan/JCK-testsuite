/*
 * @test
 * @summary JLS 15.12.4.4: an overriding instance method is selected dynamically
 *          based on the run-time class of the target object.
 * @run main DynamicDispatch
 */

public class DynamicDispatch {
    static class Animal { String sound() { return "generic"; } }
    static class Dog extends Animal {
        @Override String sound() { return "woof"; }
    }

    public static void main(String[] args) {
        Animal a = new Dog();
        if (!"woof".equals(a.sound())) throw new AssertionError("dynamic dispatch");
        if (!"generic".equals(new Animal().sound())) throw new AssertionError("base method");

        System.out.println("PASS");
    }
}
