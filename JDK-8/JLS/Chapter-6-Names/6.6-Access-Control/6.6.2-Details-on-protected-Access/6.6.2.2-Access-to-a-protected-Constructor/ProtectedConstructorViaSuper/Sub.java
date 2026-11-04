package pcv2;

import pcv1.Base;

public class Sub extends Base {
    public Sub(int v) {
        super(v);   // protected constructor accessible to a subclass via super(...)
    }
}
