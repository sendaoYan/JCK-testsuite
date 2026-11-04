# RuntimePermission — 设计方案

## 1. API 功能
`java.lang.RuntimePermission`（final，继承 `BasicPermission`）是运行时权限：构造器 `(String name)`、`(String name, String actions)`，继承 `getName/getActions/implies/equals/hashCode`。

## 2. Java 用户如何使用
- 与 `SecurityManager`/`AccessController` 配合表示权限（如 `getClassLoader`、`setSecurityManager`）。
```java
RuntimePermission p = new RuntimePermission("getClassLoader");
boolean allowed = p.implies(p);
```

## 3. 开源代码实际用法调研
- 旧式安全管理器检查点使用；JDK `test/jdk/java/lang/RuntimePermission` 验证名称/actions/implies。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证 final、父类 `BasicPermission`、两个构造器。
黑盒：名称、`BasicPermission` 忽略 actions（`getActions` 恒为空）、`implies` 同名、`equals/hashCode` 值语义。
对应 `<Simple>Test.java` 黑盒部分。
