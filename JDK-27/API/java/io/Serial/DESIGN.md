# java.io.Serial 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 注解类型 `java.io.Serial`（`@Retention(SOURCE)`、`@Target({METHOD, FIELD})`），标记"这是序列化机制的成员"以获得编译期检查：
  - 5 个方法：`writeObject/readObject/readObjectNoData/writeReplace/readResolve`。
  - 2 个字段：`serialVersionUID/serialPersistentFields`。
- 语义错误使用（非 Serializable 类上标注、Enum 上声明 UID 等）javac 告警；运行时由 SerializationMisdeclarationChecker 校验。

## 2. Java 用户如何使用
- `class A implements Serializable { @Serial private static final long serialVersionUID = 1L; @Serial private void readObject(ObjectInputStream in) ... }`。
- 仅是文档+编译期防错装置，不改变任何运行时序列化行为。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `@Serial private static final long serialVersionUID = -5926849794470754667710L;` 等 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectStreamField.java`、`InvalidClassException.java` 等 JDK 源内大量使用（grep "@Serial" java.io 包内 >30 处）。
- 注解定义与五方法/两字段清单 — `jdk25u-dev/src/java.base/share/classes/java/io/Serial.java`。

## 4. 测试用例设计 + 自动化实现（指向 SerialTest.java 黑盒 `testBlackBox`）
- 白盒: isAnnotation、@Target({METHOD,FIELD})、@Retention(SOURCE)（SOURCE 意味着运行时不可见——用 getAnnotation==null 证明）、非 Inherited。
- 黑盒场景:
  1. 声明全部 7 个被 @Serial 标注的规范成员的类**真实序列化往返**（writeReplace/readResolve 生效、v 字段相等）。
  2. @Serial 的 serialVersionUID 数值流入 ObjectStreamClass 描述符（getSerialVersionUID==24L）。
  3. 反射校验 serialVersionUID/writeObject/readObjectNoData/serialPersistentFields 的规范形态（static final long、void+IOException、ObjectStreamField[]）。
- 健壮性: SOURCE 保留策略下反射只断言"不可见"，不依赖编译器告警文本。

产出：`SerialTest.java`（白盒 8 + 黑盒 ~9 断言）。
