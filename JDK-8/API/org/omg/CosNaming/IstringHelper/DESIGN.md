# org.omg.CosNaming.IstringHelper

## API 功能
Istring（IDL string 的 typedef）Helper——NameComponent.id/kind 的实际元素类型。
## 使用方式
静态 insert/extract/read/write。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- tk_alias、content tk_string；普通/空/中文（非 ASCII）字符串 Any 往返与 CDR 往返。
