/*
 * @test
 * @summary JLS 5.1.5 (Java SE 8): a widening reference conversion exists from
 *          any reference type S to any reference type T provided S is a subtype
 *          of T. It never requires a special action at run time and never
 *          throws. Applies to class, interface, type-variable, and array types.
 * @run main WideningReferenceConversion
 */

import java.io.Serializable;
import java.util.ArrayList;

class Base {}
class Sub extends Base {}
interface I {}
class Impl extends Base implements I {}

public class WideningReferenceConversion {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Sub s = new Sub();
        Base b = s;               // class subtype -> supertype
        check(b == s, "class widening reference conversion");

        Object o = s;             // any class -> Object
        check(o == s, "class -> Object widening");

        Impl impl = new Impl();
        I i = impl;               // class -> implemented interface
        check(i == impl, "class -> interface widening");
        Base b2 = impl;           // class -> superclass
        check(b2 == impl, "class -> superclass widening");

        String[] strs = new String[1];
        Object[] objs = strs;     // array subtype -> array supertype
        check(objs == strs, "String[] -> Object[] widening");
        Object oarr = strs;       // array -> Object
        check(oarr == strs, "array -> Object widening");
        Serializable ser = strs;  // array -> Serializable
        check(ser == strs, "array -> Serializable widening");
        Cloneable cl = strs;      // array -> Cloneable
        check(cl == strs, "array -> Cloneable widening");

        // Type variable -> bound (widening reference conversion).
        check(bounded(Integer.valueOf(1)).intValue() == 1, "type variable -> bound widening");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static <T extends Number> Number bounded(T t) { return t; }
}
