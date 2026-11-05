# org.omg.DynamicAny.DynAnyOperations

## API 功能
DynAny 全操作集：type/from_any/to_any/insert_*/get_*/seek/rewind/next/component_count/current_component/copy/assign/equal/destroy。
## 使用方式
经 DynAnyFactory 实例化后调用（离线 ORB 即可）。
## 参考来源
JDK-8 rt.jar；CORBA DynamicAny 规范；行为全量 jdk8u372-b07 实测。
## 测试用例设计 + 易错细节
- 基本类型读写矩阵 + **四种 TypeMismatch 负例**（insert_string/get_string/insert_octet/insert_ulong on tk_long——无隐式加宽/符号转换）。
- from_any(42)/to_any 值与 kind 双断言；from_any 类型错 → TypeMismatch。
- 组件模型：basic count 0/seek(0) false/next false/rewind 安全；struct count 2/seek(1)/越界 seek(5) false/rewind 回首/current_component。
- copy 独立性（改副本不影响原件）、equal 动态、assign 同值、assign 异类型 TypeMismatch、equal(null)→NPE（JDK 实测）。
- **destroy 非幂等**：首次成功，其后任何操作（含二次 destroy/type/to_any）→ OBJECT_NOT_EXIST。
- wstring/any 通道 + 各自 TypeMismatch。
