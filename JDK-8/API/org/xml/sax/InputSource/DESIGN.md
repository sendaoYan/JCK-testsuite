# InputSource — 设计案

## 1. API 功能
解析输入源：字节流/字符流/systemId/publicId/encoding 五槽，4 构造器（无参/String/systemId 字节流/字符流）。

## 2. Java 用户如何使用
字节流配 encoding、字符流直读；systemId 供相对解析。

## 3. 开源代码实际用法调研
JDK test/jdk/org/xml/sax；SAX 2.0.2 javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：四构造器字段初值（流 null/systemId null）+ 全 setter/getter 往返；systemId 构造器与 setSystemId 等价；encoding 独立槽。
