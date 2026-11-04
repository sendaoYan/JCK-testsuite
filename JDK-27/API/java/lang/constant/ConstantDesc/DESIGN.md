# java.lang.constant.ConstantDesc — 设计方案

## 1. API 功能
根接口：`resolveConstantDesc(MethodHandles.Lookup) -> Object`（可抛 ReflectiveOperationException）。ClassDesc/MethodHandleDesc/MethodTypeDesc/DynamicConstantDesc 均派生。

## 2. Java 用户如何使用
```java
ConstantDesc d = ConstantDescs.NULL;      // 无类型视图
Object v = d.resolveConstantDesc(MethodHandles.lookup()); // null
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/ConstantDesc.java`。
- javac 生成 condy 时 JVM 调 bootstrap 的返回值即等价 resolve 结果。
- JDK `test/jdk/java/lang/constant/ConstantDescsTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口、唯一方法、返回 Object、异常声明、三个子接口关系。
黑盒：NULL resolve→null；TRUE/FALSE resolve；Integer 名义形式 resolve；ClassDesc 作 ConstantDesc resolve；ClassNotFound 负例（ReflectiveOperationException 子类）；泛型/函数式用法。
对应 `ConstantDescTest.java` 的 `testBlackBox()`。
