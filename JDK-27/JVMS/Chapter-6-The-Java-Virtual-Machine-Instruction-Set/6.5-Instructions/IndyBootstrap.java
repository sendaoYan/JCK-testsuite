import java.lang.invoke.*;

public class IndyBootstrap {
    public static CallSite bsm(MethodHandles.Lookup lookup, String name, MethodType type)
            throws Throwable {
        return new ConstantCallSite(
            lookup.findStatic(IndyBootstrap.class, "target", MethodType.methodType(String.class)));
    }

    public static String target() { return "indy"; }
}
