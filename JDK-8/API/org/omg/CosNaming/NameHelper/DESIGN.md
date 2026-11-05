# org.omg.CosNaming.NameHelper

## API 功能
Name（typedef sequence&lt;NameComponent&gt;，一个完整名字）Helper。
## 使用方式
静态 insert/extract/read/write；bind/resolve 等操作的名字参数类型。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- tk_alias + content tk_sequence；2 元素/1 元素/空数组 Any 与 CDR 双通道逐字段往返。
