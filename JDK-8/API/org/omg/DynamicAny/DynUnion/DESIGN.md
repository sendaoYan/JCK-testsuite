# org.omg.DynamicAny.DynUnion

## API 功能
union 的 DynAny 视图：get/set_discriminator、member/member_name/member_kind/discriminator_kind、has_no_active_member、set_to_default_member/set_to_no_active_member。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- boolean 判别 union：默认 false → arm a（tk_long）；set_discriminator(true/false) 双向切换 arm 与 kind 联动。
- 负例：异类型判别器 → TypeMismatch；无 default 分支的 set_to_default_member → TypeMismatch。
- JDK-8 宽容：无 default 也允许 set_to_no_active_member（实测记录）。
