# org.omg.CosNaming.NamingContextExtPackage.AddressHelper

## API 功能
Address（NamingContextExt 的字符串 typedef）Helper。
## 使用方式
静态 insert/extract/read/write。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id 精确串（NamingContextExt 命名空间）；tk_alias + content tk_string；普通/空串 Any 与 CDR 往返。
