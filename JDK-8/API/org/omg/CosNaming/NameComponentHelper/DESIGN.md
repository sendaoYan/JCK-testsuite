# org.omg.CosNaming.NameComponentHelper

## API 功能
NameComponent 的 IDL Helper：insert/extract(Any)、read/write(CDR 流)、type()、id()。
## 使用方式
静态方法；Any 与 OutputStream 两条 wire 通道。
## 参考来源
JDK-8 rt.jar；repository id/TypeCode 形态以 jdk8u372-b07 实测为准。
## 测试用例设计 + 易错细节
- id() 精确串 `IDL:omg.org/CosNaming/NameComponent:1.0`；type() 为 tk_struct、name()/id() 与 Helper 一致、member_count==2、成员名 id/kind、成员类型是 alias Istring(tk_alias)。
- Any 往返：extract 返回**新实例**，身份 equals 必为 false——必须逐字段断言（易错点）。
- CDR read/write 往返逐字段；空串字段存活。
- 负例：对 tk_long Any extract → org.omg.CORBA.MARSHAL。
