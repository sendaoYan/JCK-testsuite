# java.lang.invoke.MethodType — 设计方案

## 1. API 功能
不可变方法签名描述（interned，final 类）：`methodType/genericMethodType/fromMethodDescriptorString/fromDescriptor`、`changeParameterType/changeReturnType/insertParameterTypes/dropParameterTypes/appendParameterTypes`、`erase/generic/wrap/unwrap`、`toMethodDescriptorString/descriptorString/describeConstable`、`parameterType/parameterCount/parameterList/parameterArray/returnType`、`hasPrimitives/hasWrappers`。

## 2. Java 用户如何使用
```java
MethodType mt = MethodType.methodType(int.class, String.class);
MethodType g = mt.generic();      // (String)int -> (Object)Object
MethodType e = mt.erase();        // (String)int
mt == MethodType.methodType(int.class, String.class); // true: interned
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/MethodTypeTest.java`（800 行，全工厂与转换）。
- invokedynamic 引导方法第二参固定为 MethodType；StringConcatFactory/LambdaMetafactory 均依赖其描述符化。

## 4. 测试用例设计 + 自动化实现
白盒：final、interned 等价、工厂/转换方法存在。
黑盒：工厂重载一致性；genericMethodType(1,true) 尾参 Object[]；erase 保留基本型参数、generic 全 Object；wrap/unwrap 往返；fromMethodDescriptorString 往返及坏描述符异常；describeConstable 解析回同一 interned 实例；toMethodDescriptorString 精确串。
对应 `MethodTypeTest.java` 的 `testBlackBox()`。