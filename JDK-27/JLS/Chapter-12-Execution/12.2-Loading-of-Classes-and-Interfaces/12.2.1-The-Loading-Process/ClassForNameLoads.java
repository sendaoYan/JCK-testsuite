/*
 * @test
 * @summary JLS 12.2.1: Class.forName loads and initializes a class, constructing
 *          its Class object.
 * @run main ClassForNameLoads
 */

public class ClassForNameLoads {
    static class Loaded {}

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("ClassForNameLoads$Loaded");
        if (c != Loaded.class) throw new AssertionError("Class.forName result");
        System.out.println("PASS");
    }
}
