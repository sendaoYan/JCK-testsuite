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

## 深化批次（2026-11-05）
新增 `testTransformations/testNegatives/testUserImplementation/testDescParity`（旧 21 断言 → 新 63）：
- 变换代数：insertParameterTypes 多参/尾部插入、dropParameterTypes 全删与空删恒等形、changeParameterType(1,char)、changeReturnType(void)；parameterArray 防御性快照（改数组不影响原类型）；parameterList 内容 equals。
- 负例精确表：parameterType/changeParameterType 越界与负下标→ArrayIndexOutOfBoundsException；dropParameterTypes(2,1)/(0,9)→IndexOutOfBoundsException "start=2 end=1"/"start=0 end=9"；insertParameterTypes(9,…)→IndexOutOfBoundsException "bad index: 9"；changeReturnType(null)/changeParameterType(0,null)→NPE。
- 用户实现：自造 TD(OfField)+MD(OfMethod) 全 9 方法契约自实现（描述符拼装、克隆式变换）。
- MethodTypeDesc 对齐：parameterType(0)=="I"、returnType 描述符、changeParameterType(CD_long)→"(IJ)Ljava/lang/String;"、insertParameterTypes(CD_Object)、dropParameterTypes(1,2)。