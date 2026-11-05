# org.omg.DynamicAny.DynAnyPackage.InvalidValue

## API 功能
DynAny 值域违规异常（越界 ulong、未知枚举名、元素数失配等）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- idlj 异常模式：无参 getMessage()==repoId；reason 构造 == repoId + **两个空格** + reason（toString 同式）。
- Helper.type() tk_except / 名 InvalidValue / 0 成员；Any 与 CDR 往返**丢 reason**（线上仅 repoId）。
- 活体复现：DynEnum.set_as_string(未知名) 抛出且 message 精确。
