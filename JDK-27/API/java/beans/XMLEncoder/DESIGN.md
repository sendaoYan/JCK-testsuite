# java.beans.XMLEncoder 测试设计案

> 覆盖目标：完整契约（继承 Encoder + AutoCloseable、两构造器、writeObject 全类型、owner、statement/expression）。

## 1. API 功能
- 构造：`(OutputStream)`、`(OutputStream, String charset, boolean indentation, int initialSize)`。
- 写：`writeObject`、`writeStatement`、`writeExpression`、`flush`、`close`。
- owner：`setOwner`/`getOwner`。

## 2. 使用方式
- 把 bean 图写入 XML，供 `XMLDecoder` 反序列化；也可写 Statement/Expression 描述构造过程。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/XMLEncoder.html`）。
- OpenJDK `test/jdk/java/beans/XMLEncoder`（跨类 round-trip 回归）；JCK `api/java_beans/XMLEncoder`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 继承 Encoder + AutoCloseable | testStructure |
| P0 | writeObject：String/Integer/bean/数组/List/Map/null | testWriteObject |
| P1 | setOwner/getOwner | testOwner |
| P1 | 四参数构造器（charset/缩进/初始容量） | testFourArgConstructor |
| P2 | writeStatement/writeExpression | testStatements |

## 关键规范细节
- `writeObject(null)` 编码为 `<null/>`。
- bean 必须有 no-arg 构造器 + getter/setter；**不要用匿名子类**（IllegalAccessException，编码被丢弃）。
- writeStatement/writeExpression 的目标要用 bean 实例；用 String/Integer 字面量做目标会导致编码器无限递归/输出暴涨。
