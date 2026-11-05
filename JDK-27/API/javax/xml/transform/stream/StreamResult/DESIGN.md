# javax.xml.transform.stream.StreamResult — 设计案

## 1. API 功能
javax.xml.transform.stream.StreamResult 类。常量：FEATURE（共 1 个）。 实例/声明方法：setOutputStream, getOutputStream, setWriter, getWriter, setSystemId, getSystemId。

## 2. Java 用户如何使用
new StreamResult(stream/writer/systemId)。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.stream.StreamResult）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 StreamResultTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`StreamResultTest.testBlackBox()`）覆盖：三种构造 + setSystemId。

## 5. 深化（本轮新增）
- 构造矩阵 round-trip（含 (流,systemId) 双参变体；(systemId) 构造 sink 皆 null）；FEATURE 精确值；(File)/setSystemId(File) 派生 file URL。
- 真实闭环：OutputStream 路径按 UTF-8 解码与 DOC 逐字符相等；Writer 路径精确。
- 易错：断言前 replace("\r","")；不依赖平台默认字符集。
