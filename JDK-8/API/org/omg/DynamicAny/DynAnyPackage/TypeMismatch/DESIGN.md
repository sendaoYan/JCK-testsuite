# org.omg.DynamicAny.DynAnyPackage.TypeMismatch

## API 功能
DynAny 类型不匹配异常（错型判别器、错型元素、无 default 分支的 set_to_default_member 等）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 构造链/toString/Helper.type()/Any+CDR 往返丢 reason 同族模式；活体复现：无 default 分支 union 的 set_to_default_member。
