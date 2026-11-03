# javax.swing.text.html.parser.Element — DESIGN

## 1. API 功能
DTD 中元素（ELEMENT 构造）的描述（`implements DTDConstants, Serializable`）。公开字段 `index/name/oStart/oEnd/inclusions/exclusions/type/content/atts/data`。
- 查询：`getName()/omitStart()/omitEnd()/getType()/getContent()/getAttributes()/getIndex()/isEmpty()/toString()`。
- 属性查找：`getAttribute(String)`、`getAttributeByValue(String)`。
- 静态：`name2type(String)`（仅 CDATA/RCDATA/EMPTY/ANY，否则 0）。

## 2. Java 用户如何使用
`DTD.getDTD(...).getElement(name)` 或 `getElement(index)` 取得元素，读 `getName/getType/isEmpty/omitStart/omitEnd/getContent`，再 `getAttribute("align")` 取属性元数据、`getAttributeByValue("center")` 反查。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/DocumentParser.java`（自用）：`Element elem = tag.getElement(); if (elem == dtd.body) inbody++;`（用元素身份判断上下文）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/Element.java`（自用）：`getAttribute` 遍历 `atts` 链表，`getAttributeByValue` 遍历 `a.values.contains(value)`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/parser/Test8017492.java`：`DTD.getDTD("dtd").getElement("element")` 触发元素创建路径。

## 4. 测试用例设计（黑盒部分，`ElementTest.testBlackBox`）
1. `p`：name/toString、type=MODEL、非空、omitStart=false、omitEnd=true、有 content。
2. `br`：EMPTY、content==null。
3. `p.align` 属性：name/type=NMTOKEN/modifier=IMPLIED/value=null/枚举 left|center|right。
4. `getAttributeByValue("center"/"left")` 反查、未知返回 null；`getAttribute("unknown")` null。
5. `input.type` 默认值 "TEXT" 与枚举集合；`getAttributes()` 链表头。
6. `getIndex()` 与 `getElement(index)` 往返；`name2type` CDATA/RCDATA/EMPTY/ANY/MODEL/unknown。
