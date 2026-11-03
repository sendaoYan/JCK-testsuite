# javax.swing.text.StyleConstants.CharacterConstants — DESIGN

## 1. API 功能
`StyleConstants` 的嵌套 final 类，作为“字符级”属性键对象的运行时类型（`extends StyleConstants implements AttributeSet.CharacterAttribute`）。无 public 构造器，实例由 `StyleConstants` 静态初始化创建。

## 2. Java 用户如何使用
用户不直接构造；通过键对象类型判断或作为属性键使用：
```java
StyleConstants.Underline   // 是 CharacterConstants 实例，作为键
StyleConstants.setUnderline(s, true);
```

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`Underline = new CharacterConstants("underline")`、`StrikeThrough/Superscript/Subscript/ComponentAttribute/IconAttribute/BidiLevel` 同理；`CharacterConstants` 声明为 `implements AttributeSet.CharacterAttribute`，构造器 `private CharacterConstants(String)`。

## 4. 测试用例设计 + 自动化实现（`CharacterConstantsTest.testBlackBox`）
1. 无 public 构造器；`implements CharacterAttribute`。
2. 具体字符键（Underline/StrikeThrough/Superscript/Subscript/ComponentAttribute/IconAttribute/BidiLevel）是该类实例；`Bold` 不是。
3. 行为闭环：`setUnderline/setStrikeThrough` 后 `isUnderline/isStrikeThrough` 读回 true。
