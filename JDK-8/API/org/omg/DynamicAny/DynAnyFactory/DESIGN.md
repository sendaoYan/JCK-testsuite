# org.omg.DynamicAny.DynAnyFactory

## API 功能
DynAny 工厂接口：create_dyn_any(Any) / create_dyn_any_from_type_code(TypeCode)。
## 使用方式
`orb.resolve_initial_references("DynAnyFactory")` → `DynAnyFactoryHelper.narrow`（离线单进程可用，无需网络/命名服务）。
## 参考来源
JDK-8 rt.jar；CORBA 2.3 DynamicAny 规范；全部行为 jdk8u372-b07 实测。
## 测试用例设计 + 易错细节
- create_dyn_any 五类型矩阵（long/string/double/boolean/octet 值回读）+ 空 Any → tk_null。
- 负例：create_dyn_any(null) 与 create_dyn_any_from_type_code(null) → **DynAnyFactoryPackage.InconsistentTypeCode**（精确）。
- 最派生分发：enum/struct/sequence/array/union TypeCode 分别造出 DynEnum/DynStruct/DynSequence/DynArray/DynUnion（instanceof + 反例）。
- 枚举过 Any 通道（to_any → create_dyn_any）保默认成员 A。
