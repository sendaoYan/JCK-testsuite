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

## 深化批次（2026-11-05）
新增 `testStructureDeep/testMetafactoryRoundTrip/testStandardInterfaces/testAltMetafactoryFlags/testNegativeConversions`（旧 21 断言 → 新 55）：
- 签名精确化：metafactory/altMetafactory 均 throws LambdaConversionException、altMetafactory 第 4 参为 Object[] 变参尾。
- 链接闭环：CallSite.type()==()RefFn、getTarget().type() 同型、getTarget().toString()=="MethodHandle()RefFn"、产物为 ConstantCallSite、两次链接独立 CallSite。
- 标准接口：Function<String,Integer>（装箱 4/0）、Supplier（"hi"）、void+副作用计数接口（3/5 累计）。
- 捕获前缀：invokedType=(String)Fn2，invoke("pre")→apply(9)=="pre_9"，每次独立捕获。
- 旗标电池：SERIALIZABLE、MARKERS、SERIALIZABLE|MARKERS 组合、FLAG_BRIDGES 后反射 `getMethod("len",Object.class)` 桥方法 invoke=="br-ok"；非接口 marker→LCE "is not an interface"。
- 负例精确表（checkThrowsMsg 类+消息片段）：返回不兼容/参数元数/参数类型/void 实现→LCE "Type mismatch for lambda expected return"/"Incorrect number of parameters"/"Type mismatch for lambda argument 0"；publicLookup→LCE "Invalid caller"；工厂类型非接口→LCE "is not an interface"；**非直接 handle（insertArguments 产物）在 JDK-9+ 抛 LCE "not direct"（JDK-8 为 IllegalArgumentException "not a direct method handle"，分侧断言）**。
- 观察行为锁定：SAM 名不存在、双抽象方法接口链接期均不抛。
- 版本差异：JDK-27 类为 final 且无 public 构造（JDK-8 为 public class + public 无参构造，JDK-8 侧已各自断言）。