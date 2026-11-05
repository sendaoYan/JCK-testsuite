# org.omg.CosNaming.NamingContextExtHolder

## API 功能
NamingContextExtHolder（org.omg.CosNaming.NamingContextExt 对象引用）Holder。
## 使用方式
默认 value==null；_read/_write 过 CDR（nil 引用合法）；_type() tk_objref 且 id 为精确 repository id。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07（jdk8u372 nil 往返实测）。
## 测试用例设计 + 易错细节
- final + Streamable；默认 null；带 null 构造合法；nil 引用 wire 往返 → null；_type() 精确 kind 与 id。
