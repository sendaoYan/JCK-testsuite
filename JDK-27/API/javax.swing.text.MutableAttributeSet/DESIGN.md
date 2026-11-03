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
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/SimpleAttributeSet.java`：`SimpleAttributeSet implements MutableAttributeSet`；`removeAttributes(AttributeSet)` 仅删除“名字和值都匹配”的键（`value.equals(getAttribute(name))` 才删）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleContext.java`：`NamedStyle implements Style`（`Style extends MutableAttributeSet`），其 `addAttribute` 委托给 `StyleContext.addAttribute` 生成不可变共享集。

## 4. 测试用例设计 + 自动化实现（`MutableAttributeSetTest.testBlackBox`）
用 `SimpleAttributeSet` 作为 MutableAttributeSet 引用，走完整 CRUD 组合：
1. addAttribute/覆盖不增加计数/getAttribute/isDefined/containsAttribute。
2. addAttributes 批量导入。
3. removeAttribute、removeAttributes(AttributeSet) 仅删匹配键值对、removeAttributes(Enumeration)。
4. setResolveParent：getAttribute 向上回退，isDefined 保持本地语义；本地键优先。
