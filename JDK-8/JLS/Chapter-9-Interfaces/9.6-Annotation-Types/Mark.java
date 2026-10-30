import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@interface Mark {
    String value() default "dflt";
    int num() default 7;
}

@Mark
class AnnotatedBase {}

class AnnotatedSub extends AnnotatedBase {}

@Mark(value = "x", num = 9)
class ExplicitMark {}
