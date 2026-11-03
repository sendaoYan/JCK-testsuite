# java.lang.constant.DirectMethodHandleDesc — 设计方案

## 1. API 功能
直接方法句柄描述（interface，extends MethodHandleDesc）：`kind()`、`refKind()`、`isOwnerInterface()`、`owner()`、`methodName()`、`lookupDescriptor()`。

## 2. Java 用户如何使用
```java
DirectMethodHandleDesc d = MethodHandleDesc.ofMethod(Kind.VIRTUAL,
        ClassDesc.of("java.lang.String"), "length", MethodTypeDesc.of(CD_int));
MethodHandle mh = d.resolveConstantDesc(MethodHandles.lookup());
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/DirectMethodHandleDesc.java` + `DirectMethodHandleDescImpl`。
- Enum.EnumDesc/DynamicConstantDesc 内部以 BSM_*（本类型）驱动解析。
- JDK `test/jdk/java/lang/constant/DirectMethodHandleDescTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口、父接口、6 方法及返回类型。
黑盒：VIRTUAL String.length 全访问器 + refKind 5 + invocationType 插入接收者 + resolve 调用=5；INTERFACE_VIRTUAL List.size（isOwnerInterface、refKind 9）；CONSTRUCTOR `<init>` lookup 返回 V 而 invocation 返回类；STATIC_GETTER Integer.MIN_VALUE refKind 2 且 resolve 读值；GETTER refKind 1；按值 equals/hashCode；缺方法 resolve 抛 ReflectiveOperationException。
对应 `DirectMethodHandleDescTest.java` 的 `testBlackBox()`。
