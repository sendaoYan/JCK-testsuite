# Locator — 设计案

## 1. API 功能
位置接口：getPublicId/getSystemId/getLineNumber/getColumnNumber。

## 2. Java 用户如何使用
回调里取当前行列定位错误。

## 3. 开源代码实际用法调研
JDK test/jdk/org/xml/sax；SAX 2.0.2 javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：setDocumentLocator 交付活对象；换行后元素行号=2；匿名流 systemId/publicId=null；接口锁定；扩展见 ext.Locator2。
