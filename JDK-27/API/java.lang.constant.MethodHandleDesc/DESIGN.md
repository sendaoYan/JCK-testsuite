# java.lang.constant.MethodHandleDesc — 设计方案

## 1. API 功能
方法句柄描述（interface ConstantDesc）：静态工厂 `ofMethod/ofField/ofConstructor/of`；default `asType`；抽象 `invocationType`/`resolveConstantDesc`。

## 2. Java 用户如何使用
```java
MethodHandleDesc len = MethodHandleDesc.ofMethod(Kind.VIRTUAL,
        ClassDesc.of("java.lang.String"), "length", MethodTypeDesc.of(CD_int));
((MethodHandle) len.resolveConstantDesc(lookup)).invoke("hello"); // 5
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/MethodHandleDesc.java`。
- condy BSM 参数、StringConcatFactory 常量均以本类型序列化。
- JDK `test/jdk/java/lang/constant/MethodHandleDescTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口、7 方法、asType default、ofMethod 返回 DirectMethodHandleDesc。
黑盒：VIRTUAL length invocationType 含接收者并 resolve 调用；asType 适配；STATIC Integer.parseInt 无接收者并调用；STATIC_GETTER Integer.MIN_VALUE 读值；GETTER 自定义 Holder 公有字段（含接收者类型）；SETTER refKind 3；ofConstructor StringBuilder(String) invocation 返回构造类型并构造；equals/hashCode；缺方法 resolve 抛错。
对应 `MethodHandleDescTest.java` 的 `testBlackBox()`。
