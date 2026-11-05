# org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHelper

## API 功能
异常的 IDL Helper：insert/extract(Any)、read/write、type()/id()。
## 使用方式
静态方法。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id 精确串；type tk_except 且 id 与 Helper.id() 一致。
- Any/CDR 往返保类；**reason 文本不随 CDR 传输**——往返后 message 只剩 repository id（idlj 异常 marshal 只写 repoId+空 reason，易错点）。
