# java.io.Externalizable 测试方案 (DESIGN)

## 1. API 功能
- 扩展 `Serializable`，由对象完全掌控序列化形式：`writeExternal(ObjectOutput)`、`readExternal(ObjectInput)`。
- 反序列化要求 public 无参构造；`transient` 对 Externalizable 无自动语义。

## 2. Java 用户如何使用
- 手工写 `writeExternal`/`readExternal` 决定每个字段的编码；构造器被反序列化时调用。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/ObjectStreamClass.java`（`getExternalizableConstructor` 抛 "no valid constructor"）。

## 4. 测试用例设计 + 自动化实现（ExternalizableTest.java，纯黑盒）
黑盒场景:
1. 手工形式完整往返（int/UTF string/long/boolean + 有意写入的 transient 字段）。
2. 反序列化调用 public 无参构造（实例计数递增）。
3. 无 public 无参构造的 Externalizable 类反序列化抛 InvalidClassException "no valid constructor"。
4. 对象后接原始数据的混合流，readExternal 恰消耗其写出内容。
5. 经 ObjectOutput 接口多态写出。
- 健壮性: 无反射白盒；serialVersionUID 显式固定。
