# org.omg.CosNaming.BindingType

## API 功能
绑定类型枚举：nobject（普通对象）=0、ncontext（命名上下文）=1；`value()`/`from_int(int)`。
## 使用方式
常量直接使用；from_int 反查；经 BindingTypeHelper/Holder 过 Any/流。
## 参考来源
JDK-8 rt.jar idlj 枚举映射；CosNaming 规范；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 数值精确表：_nobject==0/_ncontext==1，value() 一致。
- from_int 全值归一（0→nobject、1→ncontext，`==` 同一常量）。
- 越界 from_int(5)/from_int(-1) → **org.omg.CORBA.BAD_PARAM**（vmcid 0x0 minor 0，SystemException）。
- toString 未覆写（默认形态）；类 public 非 final、实现 IDLEntity。
