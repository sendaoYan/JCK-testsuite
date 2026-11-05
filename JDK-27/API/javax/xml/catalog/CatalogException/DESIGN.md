# javax.xml.catalog.CatalogException — 设计案

## 1. API 功能
javax.xml.catalog.CatalogException 类。标记接口，无声明成员。

## 2. Java 用户如何使用
两种构造。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.catalog.CatalogException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CatalogExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CatalogExceptionTest.testBlackBox()`）覆盖：message/throwable。

## 5. 深化（本批新增）
- 构造矩阵（javap 核对）：恰好两个 —— (String) 与 (String,Throwable)；message/cause 精确。
- 层级：extends RuntimeException（unchecked）。
- 真实触发路径：RESOLVE=strict + 未匹配 systemId → CatalogException，消息含 **JAXP09040001** 与原 systemId（临时 catalog 文件 finally 删）。
