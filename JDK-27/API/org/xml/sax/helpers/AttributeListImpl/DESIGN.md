# org.xml.sax.helpers.AttributeListImpl — 设计案

## 1. API 功能
org.xml.sax.helpers.AttributeListImpl 类。实例/声明方法：setAttributeList, addAttribute, removeAttribute, clear, getLength, getName, getType, getValue。

## 2. Java 用户如何使用
addAttribute → get* → remove/clear → copy/setAttributeList。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.helpers.AttributeListImpl）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 AttributeListImplTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`AttributeListImplTest.testBlackBox()`）覆盖：增查删清 + 拷贝。

### 深化轮次2（行为契约）
黑盒：越界 getName/getType/getValue 全 null；removeAttribute(String 名字非索引)；拷贝构造独立快照；clear 只清自己。
