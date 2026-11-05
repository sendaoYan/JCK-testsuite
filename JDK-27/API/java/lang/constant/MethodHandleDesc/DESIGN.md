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

## 5. 契约深化（JDK-27 实测锁定）
- **asType 视图闭环**：`len.asType(MTD_(CharSequence)I)` resolve 后 invoke("abcd")==4（适配后仍分发原方法）；asType(null) → NPE。
- **字段写读环**：SETTER 写 Holder.x=777、GETTER 读回（经 ofField 构造）。
- **ofConstructor 无参形态**：invocationType `()Ljava/lang/Object;`，resolve 可构造。
- resolved MethodHandle 的 type().toMethodDescriptorString() 与 desc.invocationType().descriptorString() 全等。
- of(null kind)/ofConstructor(null) → NPE。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
