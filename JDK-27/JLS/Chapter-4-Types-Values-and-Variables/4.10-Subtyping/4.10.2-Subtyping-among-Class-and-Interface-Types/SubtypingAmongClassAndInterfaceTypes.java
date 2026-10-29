/*
 * @test
 * @summary JLS 4.10.2: subtyping among class and interface types. A class is a
 *          subtype of its direct superclass and superinterfaces; an interface
 *          with no direct superinterfaces has Object as a direct supertype.
 * @run main SubtypingAmongClassAndInterfaceTypes
 */

interface A {}
interface B extends A {}

class C implements B {}

class D extends C {}

public class SubtypingAmongClassAndInterfaceTypes {
    public static void main(String[] args) {
        C c = new C();
        D d = new D();

        // A class is a subtype of its direct superclass.
        if (!(d instanceof C)) throw new AssertionError("D <: C");

        // A class is a subtype of its implemented interfaces (and their supertypes).
        if (!(c instanceof B)) throw new AssertionError("C <: B");
        if (!(c instanceof A)) throw new AssertionError("C <: A (transitive interface)");

        // Every interface has Object as a supertype (Object is a supertype of all).
        Object o = c;
        if (!(o instanceof Object)) throw new AssertionError("interface <: Object");

        // An interface with no direct superinterfaces has Object as a direct supertype.
        // (The interface A has Object as a supertype.)
        A a = new C();
        Object oa = a;
        if (oa == null) throw new AssertionError("A <: Object");

        System.out.println("PASS");
    }
}
