# javax.swing.text.MutableAttributeSet — DESIGN

## 1. API 功能
可变的文本属性集合接口（`extends AttributeSet`）。核心方法：
- 增：`addAttribute(Object, Object)`、`addAttributes(AttributeSet)`。
- 删：`removeAttribute(Object)`、`removeAttributes(Enumeration<?>)`、`removeAttributes(AttributeSet)`。
- 解析父级：`setResolveParent(AttributeSet)`。

## 2. Java 用户如何使用
```java
MutableAttributeSet m = new SimpleAttributeSet();
m.addAttribute(StyleConstants.Bold, Boolean.TRUE);
m.addAttributes(otherSet);
m.setResolveParent(parent);   // 未定义的属性向上解析
m.removeAttribute(StyleConstants.Bold);
m.removeAttributes(names.elements());
```

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/SimpleAttributeSet.java`：`SimpleAttributeSet implements MutableAttributeSet`；`removeAttributes(AttributeSet)` 仅删除“名字和值都匹配”的键（`value.equals(getAttribute(name))` 才删）。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/StyleContext.java`：`NamedStyle implements Style`（`Style extends MutableAttributeSet`），其 `addAttribute` 委托给 `StyleContext.addAttribute` 生成不可变共享集。

## 4. 测试用例设计 + 自动化实现
`testTypeShape/testThroughSimpleAttributeSet/testThroughNamedStyle`（34 断言）：
- 接口方法面：AttributeSet 全部只读方法 + addAttribute/addAttributes/removeAttribute/removeAttributes(×2)/setResolveParent。
- SimpleAttributeSet 实现走查：写入后读回、remove 清除、setResolveParent 穿透与遮蔽。
- NamedStyle（StyleContext.createStyle）实现同套断言：同一接口契约在两种实现上一致成立。
- resolve parent 写入后 getResolveParent/getAttribute(ResolveAttribute) 一致。
