# StackWalker.Option — 设计方案

## 1. API 功能
`StackWalker.Option`（enum）是 `StackWalker.getInstance` 的配置选项，4 个常量：
`RETAIN_CLASS_REFERENCE`、`DROP_METHOD_INFO`、`SHOW_REFLECT_FRAMES`、`SHOW_HIDDEN_FRAMES`。

## 2. Java 用户如何使用
- 作为 `StackWalker.getInstance(Option)` / `getInstance(Set<Option>)` 参数控制栈遍历。
```java
StackWalker sw = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE));
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/StackWalker` 系列验证每个 Option 对帧信息/可见性的影响。

## 4. 测试用例设计 + 自动化实现
白盒：`Option.class.isEnum()`、继承 `Enum`。
黑盒：4 个常量存在、`values().length == 4`、`valueOf` 往返。
对应 `<Simple>Test.java` 黑盒部分。
