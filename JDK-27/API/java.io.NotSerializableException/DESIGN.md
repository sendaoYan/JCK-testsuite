# java.io.NotSerializableException 测试方案 (DESIGN)

## 1. API 功能
- `(String classname)` / `()`：writeObject 遇到不可序列化对象时抛出，message 是违规类的名字。

## 2. Java 用户如何使用
- 序列化第三方不可序列化对象时的显式错误；常见修法是把字段标记 transient 或让类实现 Serializable。

## 3. 开源代码实际用法调研
- 抛出点（message=类名）— `jdk25u-dev/src/java.base/share/classes/java/io/ObjectOutputStream.java`（writeObject0 的 NOT_SERIALIZABLE 分支）。
- 非 Serializable 祖先字段被跳过的规则 — `ObjectStreamClass` 默认字段收集（仅 Serializable 祖先的字段）。

## 4. 测试用例设计 + 自动化实现（NotSerializableExceptionTest.java）
- 白盒: extends ObjectStreamException、两构造。
- 黑盒场景:
  1. 构造器。
  2. 真实触发 1：根对象非 Serializable → message 为其类名。
  3. 真实触发 2：Serializable 类的字段值非 Serializable → message 为字段类名。
  4. 规则对照：实现 Serializable 的"普通父类子类"可序列化（祖先字段跳过，不抛）。
  5. transient 字段逃过检查。
  6. 可按 ObjectStreamException 捕获且与 NotActiveException 区分。
- 健壮性: 全内存。

产出：`NotSerializableExceptionTest.java`（白盒 4 + 黑盒 7 断言）。
