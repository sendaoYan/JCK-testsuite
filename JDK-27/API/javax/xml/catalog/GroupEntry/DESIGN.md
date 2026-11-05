# javax.xml.catalog.GroupEntry — 设计案

## 1. API 功能
OASIS catalog group 条目（<group prefer="...">）的包私有实现载体；唯一公开嵌套类型是枚举 PreferType（PUBLIC/SYSTEM），方法 prefer(String)、values/valueOf。

## 2. Java 用户如何使用
用户代码不直接接触 GroupEntry（package-private）；经 CatalogFeatures PREFER 特征与 catalog 文件中 <group prefer="public|system"> 间接生效：PREFER=public 时解析器可回退 publicId，PREFER=system 时只认 systemId。

## 3. 开源代码实际用法调研
JDK javax.xml.catalog 源码内部结构；JDK test/jdk/javax/xml/catalog 的 prefer 相关用例（group 条目 + resolveEntity 回退顺序）。

## 4. 测试用例设计 + 自动化实现
PreferTypeTest（子目录）：枚举面（2 常量、声明序 PUBLIC/SYSTEM、toString==name、valueOf 往返）；prefer(String) 矩阵（自字面量 true/对方 false/未知 false 不抛/大小写敏感）；GroupEntry 包私有反射断言。目录级补充：GroupEntry/PreferType 的行为效果在 CatalogResolverTest.testPreferSemantics() 以 PREFER=public/system 的 publicId 回退差异闭环验证。
