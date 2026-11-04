# javax.swing.text.DefaultStyledDocument.ElementSpec — DESIGN

## 1. API 功能
描述构建元素结构的一条指令（start tag / end tag / content）。方法分组：
- 常量：类型 `StartTagType/EndTagType/ContentType`；方向 `JoinPreviousDirection/JoinNextDirection/OriginateDirection/JoinFractureDirection`。
- 构造：`ElementSpec(AttributeSet, short)`、`ElementSpec(AttributeSet, short, int)`、`ElementSpec(AttributeSet, short, char[], int, int)`。
- 读写：`setType/getType`、`setDirection/getDirection`、`getAttributes`、`getArray`、`getOffset`、`getLength`。
- 调试：`toString()`。

## 2. Java 用户如何使用
`new ElementSpec(attr, ElementSpec.ContentType, text.toCharArray(), 0, len)` 描述一段内容；`setDirection(ElementSpec.JoinNextDirection)` 控制与相邻元素的合并；`getType/getDirection/getLength` 读取指令，供批量构建文档元素结构时使用。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/DefaultStyledDocument.java:707`：`new ElementSpec(attr, ElementSpec.ContentType, breakOffset - lastOffset)`。
- `src/java.desktop/share/classes/javax/swing/text/DefaultStyledDocument.java:711`：`new ElementSpec(pattr, ElementSpec.StartTagType)`。
- `src/java.desktop/share/classes/javax/swing/text/DefaultStyledDocument.java:1261`：`ElementSpec(AttributeSet a, short type, int len)` 用于解析阶段描述长度。

## 4. 测试用例设计 + 自动化实现（`ElementSpecTest.testBlackBox`）
- 校验 7 个类型/方向常量值。
- 用 char[] 构造器验证 `getType/getDirection(默认 Originate)/getAttributes/getOffset/getLength/getArray`，且 `getArray` 是防御性拷贝。
- `setType/setDirection` 往返、`toString` 输出 `StartTag:JoinNext:3`。
- 标记构造器与长度构造器：数组/属性为 null、长度/偏移为 0。
