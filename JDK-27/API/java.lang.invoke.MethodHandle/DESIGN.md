# java.lang.invoke.MethodHandle — 设计方案

## 1. API 功能
可调用类型安全引用（abstract 类）：`invokeExact/invoke/invokeWithArguments`、`type/bound/withVarargs/isVarargsCollector/asFixedArity`、`asType/asCollector/asSpreader/asVarargsCollector/bindTo`、`describeConstable/toString`。

## 2. Java 用户如何使用
```java
MethodHandle m = ...;
int n = (int) m.invokeExact("abc");       // 静态类型必须精确
Object o = m.invoke((Object) "abc");      // asType 自适应
MethodHandle b = m.bindTo("hello");
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/PublicMethodHandlesTest.java`、`BasicJavaTest`。
- javac 生成 invokedynamic 调用点全部落到 MethodHandle；Spring WebFlux/Reactor 用 asType 做适配层。

## 4. 测试用例设计 + 自动化实现
白盒：abstract 类、核心方法存在。
黑盒：invokeExact 精确类型语义；invoke 自动 asType；invokeWithArguments 参差 arity→WMTE；asType 长整型宽化；bindTo 绑定接收者；asCollector(1,int[].class,2)→(III)I；asFixedArity/asSpreader/withVarargs 类型形态；describeConstable 解析并计算；toString=="MethodHandle(int,int)int"；不可能 asType→WMTE。
对应 `MethodHandleTest.java` 的 `testBlackBox()`。