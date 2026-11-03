# java.lang.invoke.LambdaMetafactory — 设计方案

## 1. API 功能
lambda 表达式引导（final 工具类）：`metafactory(lookup, name, invokedType, samMethodType, implMethod, instantiatedMethodType)`；`altMetafactory(lookup, name, invokedType, Object...)` 支持 FLAG_SERIALIZABLE(1)/FLAG_MARKERS(2)/FLAG_BRIDGES(4) 及可选参数计数。

## 2. Java 用户如何使用
```java
CallSite cs = LambdaMetafactory.metafactory(lk, "len",
        MethodType.methodType(F.class),
        MethodType.methodType(int.class, String.class),
        impl, MethodType.methodType(int.class, String.class));
F f = (F) cs.dynamicInvoker().invokeExact(); // f.len("method")==6
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/LFCaching/`、`test/jdk/java/lang/lambda/`。
- javac 把每个 lambda 编译成 "name;invokedType" 的 metafactory invokedynamic；FLAG_* 由目标接口带 Serializable/桥方法决定。

## 4. 测试用例设计 + 自动化实现
白盒：final、无私有构造、三 FLAG 常量精确值 1/2/4、两工厂签名。
黑盒：metafactory 产出可计算实例（len==6/0）、二次调用可用；altMetafactory+FLAG_SERIALIZABLE→instanceof Serializable；FLAG_MARKERS 需带计数参数后接接口类；非接口工厂返回类型→LambdaConversionException；impl 签名不可适配→LambdaConversionException。
对应 `LambdaMetafactoryTest.java` 的 `testBlackBox()`。