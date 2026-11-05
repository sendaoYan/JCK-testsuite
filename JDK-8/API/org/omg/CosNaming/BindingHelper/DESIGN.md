# org.omg.CosNaming.BindingHelper

## API 功能
Binding 的 IDL Helper：insert/extract(Any)、read/write、type()/id()。
## 使用方式
静态方法，Any 与 CDR 双通道。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id 精确串 `IDL:omg.org/CosNaming/Binding:1.0`；type 为 tk_struct、成员名 binding_name/binding_type。
- Any/流往返逐字段（含嵌套 NameComponent 与 BindingType 常量 `==`）；extract 新实例。
- 单流两条 Binding 背靠背往返（流位置推进正确性）。
- 负例：tk_long Any 上 extract → MARSHAL。
