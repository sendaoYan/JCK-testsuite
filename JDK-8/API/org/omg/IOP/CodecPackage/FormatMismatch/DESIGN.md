# org.omg.IOP.CodecPackage.FormatMismatch 契约设计

## API 功能
final UserException（IDLEntity）：() 与 (String) 构造；decode/decode_value 失败信号。

## 参考来源
- OMG IOP Codec 异常族；jdk8u372-b07 实测。

## 测试用例设计
- final/恰 2 构造器；toString 精确（无参 "org.omg.IOP.CodecPackage.FormatMismatch: IDL:omg.org/IOP/Codec/FormatMismatch:1.0"；带消息加两空格+消息）。
- Helper wire 往返保类丢消息；Any 往返；id() 精确；type()==tk_except。

## 易错规范细节
- IDL 异常 CDR 形态只含成员字段，reason 不上线。
- 与 PI.ORBInitInfoPackage 同名异常类区分靠全限定名。
