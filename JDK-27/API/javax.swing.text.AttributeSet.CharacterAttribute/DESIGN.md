# javax.swing.text.AttributeSet.CharacterAttribute — DESIGN

## 1. API 功能
`AttributeSet` 的嵌套空标记接口，用于给“字符级呈现”相关的属性键打类型标签（不闭合集合，可随版本扩展、用户可新增）。无任何方法。

## 2. Java 用户如何使用
用户以 `instanceof AttributeSet.CharacterAttribute` 判断某个属性键是否影响字符级呈现：
```java
if (key instanceof AttributeSet.CharacterAttribute) { /* 字符级样式 */ }
```

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AttributeSet.java`：`public interface CharacterAttribute {}`（空接口）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`Underline/StrikeThrough/Superscript/Subscript/ComponentAttribute/IconAttribute` 均 `new CharacterConstants(...)`，`BidiLevel = new CharacterConstants("bidiLevel")`；`FontConstants`、`ColorConstants` 也各自 `implements CharacterAttribute`。

## 4. 测试用例设计 + 自动化实现（`CharacterAttributeTest.testBlackBox`）
1. 接口无方法、无字段（空标记）。
2. `StyleConstants$CharacterConstants` 实现该标记（`isAssignableFrom`）。
3. 具体字符键实例归属断言：`Underline/StrikeThrough/Superscript/Subscript/ComponentAttribute/IconAttribute` 是 CharacterAttribute 实例；`Alignment` 不是。
4. `SimpleAttributeSet` 不实现该标记。
5. 行为闭环：把 `StyleConstants.Underline` 作为键存入 `SimpleAttributeSet`，`StyleConstants.isUnderline` 通过该键读回 true。
