# javax.swing.text.html.parser.AttributeList — DESIGN

## 1. API 功能
SGML DTD 中元素属性（ATTLIST 构造）的描述，本质是属性链表的一个节点（`implements DTDConstants, Serializable`）。公开字段：`name/type/values/modifier/value/next`。
- 构造器：`AttributeList(String name)`、`AttributeList(String name, int type, int modifier, String value, Vector<?> values, AttributeList next)`。
- 查询：`getName()/getType()/getModifier()/getValue()/getValues()/getNext()/toString()`（toString 返回 name）。
- 静态类型名互转：`name2type(String)`、`type2name(int)`（CDATA/ID/NMTOKEN/NUMBER 及 fixed/required/implied 等）。

## 2. Java 用户如何使用
通常不直接 new，而是从 DTD 的 `Element.getAttribute(name)` 或 `getAttributes()` 链上取得，然后 `getName/getType/getModifier/getValues` 读元数据，`getNext()` 遍历链表。`name2type`/`type2name` 用于在属性类型名和 DTDConstants 常量间互转。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/DTD.java`（自用）：
  ```java
  return new AttributeList(name, type, modifier, value, vals, atts);
  ```
  （`defAttributeList` 用 `StringTokenizer(values, "|")` 切分枚举值后构造）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/Element.java`（自用）：
  ```java
  public AttributeList getAttribute(String name) {
      for (AttributeList a = atts ; a != null ; a = a.next)
          if (a.name.equals(name)) return a;
      return null;
  }
  ```
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/AttributeList.java`（自用）：静态块 `defineAttributeType("CDATA", CDATA)` 建双向 name<->type 表。

## 4. 测试用例设计（黑盒部分，`AttributeListTest.testBlackBox`）
1. `name2type`：CDATA/ID/NMTOKEN/NUMBER/fixed/required/implied 精确值 + 未知名回退 CDATA；`type2name`：CDATA/NMTOKEN/ID 回名 + 未知值返回 null。
2. 简单构造器：name 往返、type/modifier/value/values/next 默认值、toString==name。
3. 全参构造器 + `next` 链表：字段往返、`getValues()` 枚举顺序、链表 tail。
4. 从真实 html32 DTD 取 `p.align`：name/type=NMTOKEN/modifier=IMPLIED/value=null/枚举值 left|center|right、单属性链表。
