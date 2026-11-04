# java.io.Serializable 测试方案 (DESIGN)

## 1. API 功能
- 标记接口（无方法、无常量）：使类的实例可被 `ObjectOutputStream` 序列化、`ObjectInputStream` 反序列化。
- 数组类型一律 Serializable；`Externalizable` 扩展 Serializable。

## 2. Java 用户如何使用
- `class X implements Serializable` + `serialVersionUID`；非序列化字段加 `transient`；对象图可写读往返。

## 3. 开源代码实际用法调研
- 语义 — `jdk8u-dev/jdk/src/share/classes/java/io/Serializable.java`、`ObjectOutputStream/ObjectInputStream`。

## 4. 测试用例设计 + 自动化实现（SerializableTest.java，纯黑盒）
黑盒场景:
1. JDK 类型可序列化性判定（String/Integer/ArrayList/LocalDate 是，Scanner/Thread/Object 否）。
2. 数组类型（int[]/Object[]/int[][]）皆 Serializable。
3. String 与 int[] 经标记契约往返；普通 Object 写抛 NotSerializableException 且消息为类名。
4. 枚举常量往返且同一性保持。
5. 用户标记类、无字段类、继承的 Serializable 子类往返。
- 健壮性: 无反射白盒；serialVersionUID 显式固定。
