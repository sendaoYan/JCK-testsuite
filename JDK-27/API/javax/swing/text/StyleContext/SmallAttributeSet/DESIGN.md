# javax.swing.text.StyleContext.SmallAttributeSet — DESIGN

## 1. API 功能
`StyleContext` 的非静态内部类，不可变、可共享的 `AttributeSet` 实现，用扁平 `Object[]`（key,value,key,value…）存储少量属性。核心方法：
- 构造：`SmallAttributeSet(Object[] attributes)`、`SmallAttributeSet(AttributeSet attrs)`。
- 查询：`getAttributeCount/getAttribute/getAttributeNames/isDefined/containsAttribute/containsAttributes/isEqual/copyAttributes/getResolveParent`。
- 对象方法：`toString/hashCode/equals/clone`（clone/copyAttributes 返回 this，不可变）。

## 2. Java 用户如何使用
通常由 `StyleContext` 内部创建；用户也可直接构造：
```java
StyleContext sc = new StyleContext();
StyleContext.SmallAttributeSet s = sc.new SmallAttributeSet(new Object[]{ StyleConstants.Bold, Boolean.TRUE });
boolean b = Boolean.TRUE.equals(s.getAttribute(StyleConstants.Bold));
```

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleContext.java`：`SmallAttributeSet(AttributeSet)` 先 `new Object[2*n]` 再枚举复制；`updateResolveParent` 从数组中提取 `ResolveAttribute`；`getAttribute` 先本地后 parent；`equals` 比较 count + containsAttributes；`clone()`/`copyAttributes()` 返回 this。

## 4. 测试用例设计 + 自动化实现（`SmallAttributeSetTest.testBlackBox`）
1. 从 key/value 数组构造：count、getAttribute、isDefined、containsAttribute、getAttributeNames。
2. containsAttributes 子集判断。
3. 值等价（equals/isEqual 自反与对称）、clone/copyAttributes 返回 this、无 parent。
4. 从 AttributeSet 构造的复制。
5. resolve parent 从源集 ResolveAttribute 携带并解析。
