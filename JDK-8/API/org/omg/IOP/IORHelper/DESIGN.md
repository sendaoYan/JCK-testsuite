# org.omg.IOP.IORHelper 契约设计

## API 功能
IDL Helper：insert/extract(Any)、read/write(CDR)、type()、id()。

## 参考来源
- OMG IDL/Java 映射 Helper 规范；jdk8u372-b07 离线 CDR 实测。

## 测试用例设计
- id() 精确 "IDL:omg.org/IOP/IOR:1.0"；type() tk_struct，member_count==2，member_name(0/1) 精确。
- write→read 往返：type_id/profiles 全等。
- Any insert/extract 往返；insert(any,null)→NPE；extract(空 Any)→MARSHAL。

## 易错规范细节
- type() 返回的 TypeCode struct 成员名即字段名（type_id/profiles）。
