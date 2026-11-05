# javax.xml.stream.events.Attribute — 设计案

## 1. API 功能
属性接口（extends XMLEvent）。方法：getName（QName）、getValue、getDTDType、isSpecified。事件码 ATTRIBUTE=10。

## 2. Java 用户如何使用
工厂 createAttribute(local,value)/createAttribute(QName,value)/createAttribute(prefix, namespaceURI, local, value)；
解析：作为 StartElement.getAttributes() 迭代项出现。

## 3. 开源代码实际用法调研
JDK 实现 AttributeEvent；JDK test/jdk javax/xml/stream；本仓库 AttributeTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 工厂 (local,value)：QName ns=""/prefix=""；getDTDType 默认 "CDATA"；**isSpecified()=false（JDK 实现，
  与解析侧 true 相反）**；事件码 10、isAttribute()=T、isNamespace()=F。
- 工厂 (prefix, nsURI, local, value)：四参顺序是 prefix 在前（易写反）。
- 工厂 (QName,value)：原样保留 QName。
- createAttribute((String)null, v) → IllegalArgumentException。
- 解析侧：`<r a='1' b='2'>` 属性按文档序、每项 isSpecified()=true、getDTDType "CDATA"、事件码 10；
  带前缀属性 QName 三元组精确；reader 从不把属性作为独立事件产出（无码 10 的 nextEvent）。
- getAttributeByName(QName) 命中返回同一值、未命中 null。
- 编码：作为工厂 SE 子项输出 `k='v'` 形态。
