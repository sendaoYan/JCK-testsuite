# java.security.GuardedObject 黑盒行为测试设计

## 1. API 功能
- 构造：`GuardedObject(Object, Guard)`。
- 查询：`getObject()`（Guard 拒绝时抛 SecurityException）；实现 `Serializable`。

## 2. Java 用户如何使用
把敏感对象交给 Guard 保护，`getObject()` 在 Guard.checkGuard 通过时返回对象，否则抛 SecurityException。
```java
GuardedObject go = new GuardedObject(secret, guard);
Object secret = go.getObject(); // 失败则抛 SecurityException
```

## 3. 开源代码实际用法调研
- 实现权威：`src/java.base/share/classes/java/security/GuardedObject.java`、`Guard.java`。
- `test/jdk/java/security/...` 中 Guard/GuardedObject 主要用于权限保护（Permission 实现 Guard）。

## 4. 测试用例设计 + 自动化实现
- 自定义允许 Guard：getObject 返回原对象，可重复调用。
- 自定义拒绝 Guard：getObject 抛 SecurityException，且 message 精确断言。
- 白盒：concrete + Serializable，含 getObject。
