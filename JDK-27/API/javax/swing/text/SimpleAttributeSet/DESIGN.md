# javax.swing.text.SimpleAttributeSet — DESIGN

## 1. API 功能
`MutableAttributeSet` 的直接实现（`implements MutableAttributeSet, Serializable, Cloneable`），底层用 `LinkedHashMap` 存键值。核心方法分组：
- 构造：`SimpleAttributeSet()`、`SimpleAttributeSet(AttributeSet source)`。
- 查询：`isEmpty()`、`getAttributeCount()`、`isDefined`、`getAttribute`、`getAttributeNames`、`containsAttribute`、`containsAttributes`、`isEqual`。
- 修改：`addAttribute`、`addAttributes`、`removeAttribute`、`removeAttributes(Enumeration)`、`removeAttributes(AttributeSet)`、`setResolveParent`。
- 复制/比较：`copyAttributes()`、`clone()`、`equals`、`hashCode`、`toString`。
- 常量：`EMPTY`（不可变空集合，`EmptyAttributeSet`）。

## 2. Java 用户如何使用
```java
SimpleAttributeSet s = new SimpleAttributeSet();
s.addAttribute(StyleConstants.Bold, Boolean.TRUE);
SimpleAttributeSet copy = new SimpleAttributeSet(s);
boolean eq = s.equals(copy);          // 按键值等价
SimpleAttributeSet cloned = (SimpleAttributeSet) s.clone();
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/SimpleAttributeSet.java`：`table = new LinkedHashMap<>(3)`；`getAttribute` 先本地后 `getResolveParent().getAttribute`；`containsAttributes` 逐一比较；`clone` 复制 map。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/DefaultStyledDocument/DocNegLenCharAttrTest.java`：`SimpleAttributeSet attr = new SimpleAttributeSet(); StyleConstants.setComponent(attr, lbl);`。

## 4. 测试用例设计 + 自动化实现（`SimpleAttributeSetTest.testBlackBox`）
1. 空集语义 + add/覆盖/remove 的完整链路。
2. getAttributeNames 计数与含键断言（排序无关，用 contains）。
3. 复制构造、equals/isEqual、clone/copyAttributes 的“等价但不同实例”。
4. containsAttributes 子集判断。
5. resolve parent 回退与本地 isDefined 语义。
6. `EMPTY` 常量行为。
