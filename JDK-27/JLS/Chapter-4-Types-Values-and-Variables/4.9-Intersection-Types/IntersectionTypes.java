/*
 * @test
 * @summary JLS 4.9: an intersection type T1 & ... & Tn has as values those
 *          objects that are values of all the types Ti. It arises from type
 *          parameter bounds and cast expressions; its members are the members
 *          of the notional class/interface it induces.
 * @run main IntersectionTypes
 */

interface HasName {
    String name();
}

class Base {
    String base() { return "base"; }
}

class Child extends Base implements HasName {
    public String name() { return "child"; }
}

public class IntersectionTypes {
    // T's bound is the intersection type Base & HasName.
    static <T extends Base & HasName> String describe(T t) {
        return t.base() + ":" + t.name();
    }

    public static void main(String[] args) {
        Child c = new Child();

        // Intersection type derived from a type parameter bound.
        if (!describe(c).equals("base:child")) {
            throw new AssertionError("members of intersection bound");
        }

        // Intersection type derived from a cast expression.
        Object o = c;
        Base b = (Base & HasName) o;   // cast to intersection type
        if (!b.base().equals("base")) throw new AssertionError("cast to intersection type");
        HasName h = (Base & HasName) o;
        if (!h.name().equals("child")) throw new AssertionError("intersection cast member");

        // The values of the intersection type are values of all constituent types.
        if (!(o instanceof Base)) throw new AssertionError("must be a Base");
        if (!(o instanceof HasName)) throw new AssertionError("must be a HasName");

        System.out.println("PASS");
    }
}
