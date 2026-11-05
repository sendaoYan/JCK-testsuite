# org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode

## API 功能
工厂收到无法构建 DynAny 的 TypeCode 时抛出。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 构造链/toString/Helper.type() 同族；活体：`create_dyn_any_from_type_code(get_primitive_tc(tk_except))` → 本异常；
  而**良构** exception tc（create_exception_tc 带 id/name）被接受为 count=0 叶子 DynAny（对照记录）。
