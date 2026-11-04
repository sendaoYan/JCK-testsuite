# java.io.ObjectStreamField 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(String name, Class<?> type)`、`(String name, Class<?> type, boolean unshared)`。
- 描述查询：`getName()`、`getType()`、`getTypeCode()`（JVM 签名字母 B/C/D/F/I/J/S/Z/L/[）、`getTypeString()`（基本类型为 null，引用为 JVM 签名）、`isPrimitive()`、`isUnshared()`。
- 偏移：`getOffset()`/`setOffset(int)`（protected setOffset）。
- 排序/展示：`Comparable.compareTo`（基本类型在前，同类别按名字典序）、`toString()`（`<签名> <名字>`）。

## 2. Java 用户如何使用
- 在 `private static final ObjectStreamField[] serialPersistentFields` 中显式声明落盘字段（比 transient 更强的形态控制）。
- 解析/生成类描述符的工具按 `getTypeCode`/`getTypeString` 重建字段表；`compareTo` 用于把字段按线格式规范排序。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `new ObjectStreamField("s", String.class), new ObjectStreamField("i", int.class), new ObjectStreamField("l", long.class)`（serialPersistentFields 声明形态）— `jdk25u-dev/test/jdk/java/io/Serializable/records/SerialPersistentFieldsTest.java:88-90`。
- `ObjectStreamClass.getFields()` 返回的描述符即本类型 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectStreamClass.java`（字段表构造）。
- compareTo 排序契约 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectStreamField.java` `compareTo` 实现（prim 先、按 name）。

## 4. 测试用例设计 + 自动化实现（指向 ObjectStreamFieldTest.java 黑盒 `testBlackBox`）
- 白盒: 具体类、Comparable、两个公共构造器、方法面。
- 黑盒场景:
  1. int/long/String/int[]/double/unshared 描述符的 name/type/typeCode/typeString/isPrimitive/isUnshared 全套精确断言（typeCode 用 JVM 字母）。
  2. offset 初值 0 + 匿名子类调 protected setOffset 生效。
  3. compareTo：相等=0、基本<引用、名字序、kind 优先于名字。
  4. toString 三形态（`I x`、`Ljava/lang/String; y`、`[I w`）。
  5. **真实驱动**：serialPersistentFields 用 ObjectStreamField 数组声明 → 序列化往返只落声明字段（transient 与未声明字段不落盘）。
- 健壮性: 全部固定字面量断言，无环境依赖。

产出：`ObjectStreamFieldTest.java`（白盒 ~14 + 黑盒 ~24 断言）。
