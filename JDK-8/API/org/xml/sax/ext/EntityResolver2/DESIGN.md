# EntityResolver2 — 设计案

## 1. API 功能
实体解析扩展：getExternalSubset(name,baseURI)/resolveEntity(name,pid,baseURI,sid) 四参 + 继承两参 resolveEntity。

## 2. Java 用户如何使用
带实体名与 baseURI 的精确外部子集/实体拦截。

## 3. 开源代码实际用法调研
SAX 2.0.2 extensions javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：外部 DTD 子集引用走四参 resolveEntity(name=null, systemId)；继承关系 EntityResolver2 extends EntityResolver；返回 null → 默认解析（FileNotFoundException）；纯 EntityResolver 两参通道对照。
