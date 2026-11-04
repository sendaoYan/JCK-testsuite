# java.io.NotSerializableException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 ObjectStreamException；消息为不可序列化类名。

## 2. Java 用户如何使用
- writeObject 遇到不可序列化对象图时捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/NotSerializableException.java`；`ObjectOutputStream` 抛出。

## 4. 测试用例设计 + 自动化实现（NotSerializableExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息。
2. 根对象不可序列化 → 消息为类名。
3. 可序列化类持有不可序列化字段 → 消息为字段类名。
4. 不可序列化父类的可序列化子类可正常序列化（对比）。
5. transient 字段跳过检查（对比）。
6. 作为 ObjectStreamException 捕获且区别于 NotActiveException。
- 健壮性: 无反射白盒。
