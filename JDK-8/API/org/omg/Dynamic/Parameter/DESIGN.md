# org.omg.Dynamic.Parameter

## API 功能
DSI 服务端请求参数条目：`Any argument` + `org.omg.CORBA.ParameterMode mode`（IDL struct）。
## 使用方式
`new Parameter()`（null/null）或全参构造；字段直接读写。JDK-8 的 org.omg.Dynamic 仅含此类（无 Request/Helper——rt.jar 实证）。
## 参考来源
JDK-8 rt.jar（org/omg/Dynamic/Parameter.class 唯一）；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- final + IDLEntity；默认 null/null；Any 引用原样保存；字段可变可置 null。
- equals/hashCode/toString 均**未覆写**（身份语义——与 CosNaming 结构类一致）。
- ParameterMode 三常量 {0,1,2} 精确表 + from_int 归一 + from_int(9)→BAD_PARAM 边界；三 mode 逐一作为字段值回读。
