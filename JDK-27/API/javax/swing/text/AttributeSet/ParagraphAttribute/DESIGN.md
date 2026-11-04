# javax.swing.text.AttributeSet.ParagraphAttribute — DESIGN

## 1. API 功能
`AttributeSet` 的嵌套空标记接口，用于给“段落级呈现”相关的属性键打类型标签。无任何方法。

## 2. Java 用户如何使用
用户以 `instanceof AttributeSet.ParagraphAttribute` 判断属性键是否影响段落呈现：
```java
if (key instanceof AttributeSet.ParagraphAttribute) { /* 段落样式 */ }
```

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AttributeSet.java`：`public interface ParagraphAttribute {}`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`FirstLineIndent/LeftIndent/RightIndent/LineSpacing/SpaceAbove/SpaceBelow/Alignment` 均 `new ParagraphConstants(...)`。

## 4. 测试用例设计 + 自动化实现（`ParagraphAttributeTest.testBlackBox`）
1. 接口无方法、无字段（空标记）。
2. `StyleConstants$ParagraphConstants` 实现该标记。
3. `Alignment/FirstLineIndent/LeftIndent/RightIndent/LineSpacing/SpaceAbove/SpaceBelow` 是 ParagraphAttribute 实例；`Bold` 不是。
4. `SimpleAttributeSet` 不实现该标记。
5. 行为闭环：`setAlignment(s, ALIGN_RIGHT)` 后 `getAlignment` 读回 ALIGN_RIGHT。
