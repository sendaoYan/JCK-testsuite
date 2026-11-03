# javax.xml.crypto.OctetStreamData — 设计案

## 1. API 功能
javax.xml.crypto.OctetStreamData 类。实例/声明方法：getOctetStream, getURI, getMimeType。

## 2. Java 用户如何使用
两个构造器 → getURI/getMimeType/getOctetStream 精确；单参构造默认 URI/MIME 为 null。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.OctetStreamData）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 OctetStreamDataTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`OctetStreamDataTest.testBlackBox()`）覆盖：默认字段 null、双参字段、字节流可读且 getter 稳定。。
