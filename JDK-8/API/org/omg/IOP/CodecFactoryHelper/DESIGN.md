# org.omg.IOP.CodecFactoryHelper 契约设计

## API 功能
objref Helper：narrow/unchecked_narrow/insert/extract/type/id。

## 参考来源
- OMG IOP CodecFactory；jdk8u372-b07 离线实测。

## 测试用例设计
- id() 精确；type().kind()==tk_objref。
- narrow(null)==null；unchecked_narrow(null)==null。
- 离线闭环：resolve_initial_references("CodecFactory")+narrow 非 null+create_codec 成功。
- 负例：extract(空 Any)→MARSHAL；insert(本地对象)→MARSHAL（write_Object 拒绝 local object）。

## 易错规范细节
- 离线 ORB 的 CodecFactory 是 LocalObject：不能过 Any/CDR 通道封送，但可本地调用。
