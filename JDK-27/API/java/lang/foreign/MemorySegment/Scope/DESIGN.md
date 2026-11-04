# MemorySegment.Scope — 设计方案

## 1. API 功能
`java.lang.foreign.MemorySegment.Scope`（接口）表示段的生命周期作用域。`isAlive()` 判断作用域是否仍有效；`equals`/`hashCode` 支持身份比较。

## 2. Java 用户如何使用
```java
MemorySegment.Scope scope = segment.scope();
if (!scope.isAlive()) { /* 段已失效，不可访问 */ }
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestSegments` 断言 `scope().isAlive()` 随 arena close 翻转。
- `~/git/jdk25u-dev` `ArenaImpl` 中 scope 随 close 标记不可用。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + isAlive/equals/hashCode 签名。
黑盒：scope 初始 alive；同一 arena 段共享同一 scope；不同 arena 不同 scope；close 后 isAlive false；hashCode 稳定。
对应 `ScopeTest.java` 白盒 + 黑盒部分。
