# org.omg.CosNaming.BindingListHelper

## API 功能
BindingList（typedef sequence&lt;Binding&gt;）Helper：数组 insert/extract、read/write、type()/id()。
## 使用方式
静态方法；NamingContext.list() 的出参类型。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- type 为 **tk_alias**（name BindingList），content 为 tk_sequence——Holder 的 _type() 同为 alias。
- 2 元素数组 Any/CDR 双通道逐元素往返；**空数组**两通道均存活（长度前缀 0）；单元素往返。
