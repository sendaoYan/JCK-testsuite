# java.lang.invoke.ConstantCallSite — 设计方案

## 1. API 功能
不可变调用点（CallSite final 子类）：构造后 target 永久固定，`setTarget` 抛 UnsupportedOperationException；`dynamicInvoker` 缓存。

## 2. Java 用户如何使用
```java
ConstantCallSite cs = new ConstantCallSite(MethodHandles.constant(String.class, "x"));
cs.setTarget(other); // UnsupportedOperationException
```

## 3. 开源代码实际用法调研
- JDK `ConstantCallSite.java` Javadoc 与 `test/jdk/java/lang/invoke/ConstantCallSiteTest.java`。
- LambdaMetafactory（非可序列化路径）与大部分 bootstrap 返回该类型以允许 JIT 完全内联。

## 4. 测试用例设计 + 自动化实现
白盒：final、超类 CallSite、公有构造 2 个（target/target+MethodType）。
黑盒：type 从 target 推导；setTarget→UOE 且 target 不变；引用类型站点创建与调用正确；dynamicInvoker 与 getTarget() 结果一致。
对应 `ConstantCallSiteTest.java` 的 `testBlackBox()`。