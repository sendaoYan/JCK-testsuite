# java.lang.invoke.VarHandle.VarHandleDesc — 设计方案

## 1. API 功能
VarHandle 名义描述（VarHandle 嵌套 final 类，实现 ConstantDesc）：`ofField/ofStaticField/ofArray` 工厂、`varType/coordinateTypes`、`equals/hashCode/toString`、`resolveConstantDesc`。

## 2. Java 用户如何使用
```java
VarHandleDesc d = VarHandleDesc.ofField(ClassDesc.of("Point"), "x", CD_int);
VarHandle vh = d.resolveConstantDesc(MethodHandles.lookup());
vh.set(p, 8);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/VarHandleDescTest.java` 与 condy 常量池序列化。
- javac 把 `ldc VarHandle`（字段/数组常量）编码为 VarHandleDesc 常量。

## 4. 测试用例设计 + 自动化实现
白盒：final、实现 ConstantDesc、三工厂静态。
黑盒：ofField 与现场 describeConstable equals；resolve 得到可写句柄（set x=8）；ofStaticField get/set；ofArray 读写；varType/coordinateTypes 精确；值语义 equals/hashCode；ofField null 检查。
对应 `VarHandleDescTest.java` 的 `testBlackBox()`。