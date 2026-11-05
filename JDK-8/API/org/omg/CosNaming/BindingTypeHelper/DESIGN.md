# org.omg.CosNaming.BindingTypeHelper

## API 功能
BindingType 枚举 Helper：insert/extract、read/write、type()/id()。
## 使用方式
静态方法。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id 精确串；type 为 tk_enum、member_count==2、成员名 nobject/ncontext（枚举 TypeCode 可查成员）。
- Any/CDR 往返均**归一到同一常量对象**（==）。
- 负例与结构类不同：tk_long Any 上 extract → **BAD_PARAM**（非 MARSHAL）。
