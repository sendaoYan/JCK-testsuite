# org.omg.CosNaming.NameComponent

## API 功能
CORBA CosNaming 命名服务的名字最小单元：`String id` + `String kind` 两个公共字段（IDL struct NameComponent）。
## 使用方式
`new NameComponent()`（null/null）或 `new NameComponent(id, kind)`；字段直接读写；经 NameComponentHelper/Holder 过 Any/CDR 流。
## 参考来源
JDK-8 rt.jar idlj 生成类；CORBA 2.3 CosNaming 规范；行为以 jdk8u372-b07 实测为准。
## 测试用例设计 + 易错细节
- 构造：默认构造 id==kind==null；全参构造逐字段回读（含 null id/null kind 边界）；String 引用原样保存（无防御拷贝，`==` 断言）。
- **equals/hashCode 未被覆写（身份语义）**——JDK 的 CosNaming 映射不做值相等：同实例 true、同字段异实例 false、null/异类型 false、hashCode==identityHashCode。
- toString 未覆写（`ClassName@hex` 前缀断言，不锁死十六进制位）。
- 类 final、实现 IDLEntity/Serializable；字段可变且可置回 null。
