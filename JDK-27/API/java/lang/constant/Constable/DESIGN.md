# java.lang.constant.Constable — 设计方案

## 1. API 功能
单方法接口 `describeConstable() -> Optional<? extends ConstantDesc>`：能表示为常量池条目的类型实现之；空 Optional 表示无名义形式。

## 2. Java 用户如何使用
```java
Optional<? extends ConstantDesc> d = Integer.valueOf(5).describeConstable();
Object back = d.orElseThrow().resolveConstantDesc(MethodHandles.lookup());
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/Constable.java`。
- Integer/String/Class/MethodType/MethodHandle/Enum 的实现（返回各自的 *Desc）。
- javac `condy` 生成：字符串拼接、records 的 ObjectMethods 均消费 describeConstable 结果。

## 4. 测试用例设计 + 自动化实现
白盒：接口、唯一抽象方法、Optional 返回；Integer/String/Class/MethodType 是实现者。
黑盒：9 种包装+String 常量 round-trip；Class→ClassDesc→resolve 同一性；MethodType→MethodTypeDesc；枚举常量 describe+resolve；用户 Constable 提供现成常量（Answer→42）；无名义形式返回 empty Optional；新建 String 仍可描述。
对应 `ConstableTest.java` 的 `testBlackBox()`。
