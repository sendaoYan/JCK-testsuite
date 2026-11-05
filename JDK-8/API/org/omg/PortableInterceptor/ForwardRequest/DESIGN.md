# org.omg.PortableInterceptor.ForwardRequest 契约设计

## API 功能
final UserException：成员 `org.omg.CORBA.Object forward`；()/  (Object)/  (String,Object) 三构造。

## 参考来源
- OMG PI 规范；jdk8u372-b07 实测。

## 测试用例设计
- final/恰 3 构造器/forward 类型与默认 null。
- toString 精确（repo id 前缀 + 两空格消息）。
- Helper wire 往返（null forward 可过流；reason 丢失）；Any 往返。
- 负例：forward 为本地对象（离线 CodecFactory）→ MARSHAL（write_Object 拒绝 local object）。

## 易错规范细节
- wire 只封送成员不封送 reason；本地对象引用不可封送。
