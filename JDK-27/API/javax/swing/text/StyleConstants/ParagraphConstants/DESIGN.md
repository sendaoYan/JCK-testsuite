# javax.swing.text.StyleConstants.ParagraphConstants — DESIGN

## 1. API 功能
`StyleConstants` 的嵌套 final 类，作为“段落级”属性键对象的运行时类型（`extends StyleConstants implements AttributeSet.ParagraphAttribute`）。无 public 构造器。

## 2. Java 用户如何使用
```java
StyleConstants.Alignment / FirstLineIndent / LeftIndent / ...   // ParagraphConstants 实例，作为键
StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
```

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`FirstLineIndent/LeftIndent/RightIndent/LineSpacing/SpaceAbove/SpaceBelow/Alignment` 均 `new ParagraphConstants(...)`；`ParagraphConstants` 声明 `implements AttributeSet.ParagraphAttribute`，构造器 `private ParagraphConstants(String)`。

## 4. 测试用例设计 + 自动化实现（`ParagraphConstantsTest.testBlackBox`）
1. 无 public 构造器；`implements ParagraphAttribute`。
2. `Alignment/FirstLineIndent/LeftIndent/RightIndent/LineSpacing/SpaceAbove/SpaceBelow` 是该类实例；`Bold` 不是。
3. 行为闭环：`setAlignment(ALIGN_CENTER)/setLeftIndent(8.0f)` 后读回正确值。
