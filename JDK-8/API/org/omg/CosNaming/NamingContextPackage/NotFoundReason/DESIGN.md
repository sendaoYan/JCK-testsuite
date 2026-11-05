# org.omg.CosNaming.NamingContextPackage.NotFoundReason

## API 功能
NotFound 异常的原因枚举：missing_node=0 / not_context=1 / not_object=2。
## 使用方式
常量字段 + value()/from_int(int)；作为 NotFound.why 字段的取值。
## 参考来源
JDK-8 rt.jar idlj 枚举映射；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 三值精确表 + from_int 全值归一（== 同一常量对象）。
- 越界 from_int(3)/from_int(-1) → org.omg.CORBA.BAD_PARAM（与 BindingType 同模式）。
- 类 public 非 final、实现 IDLEntity；toString 默认形态。
