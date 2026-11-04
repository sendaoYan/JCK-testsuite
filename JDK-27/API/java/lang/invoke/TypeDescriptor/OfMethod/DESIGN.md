# java.lang.invoke.TypeDescriptor.OfMethod — 设计方案

## 1. API 功能
方法类型描述符（TypeDescriptor 嵌套接口）：`changeReturnType/changeParameterType/dropParameterTypes/insertParameterTypes/parameterList/parameterArray/parameterCount/returnType/descriptorString`；MethodType 与 MethodTypeDesc 双实现。

## 2. Java 用户如何使用
```java
OfMethod<?, ?> m = MethodType.methodType(int.class, String.class);
OfMethod<?, ?> v = m.changeReturnType(void.class); // (String)void
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/TypeDescriptorTest.java`（OfMethod 部分）。
- 两个实现使 invokedynamic 框架代码可同时接受运行时 MethodType 与 nominal MethodTypeDesc。

## 4. 测试用例设计 + 自动化实现
白盒：嵌套接口、9 方法声明。
黑盒：MethodType 与 MethodTypeDesc 的参数/返回/计数一致；changeReturnType/changeParameterType/dropParameterTypes/insertParameterTypes 在两实现上产出相同 descriptorString；泛型捕获转换（cast OfMethod<ClassDesc,?> 后 CD_int 变换）；原对象不被改动。
对应 `OfMethodTest.java` 的 `testBlackBox()`。