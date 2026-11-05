# org.omg.CORBA.Principal 契约覆盖设计（仅 JDK-8）

## API 功能
已废弃的鉴别主体占位类（@Deprecated、具体类）：name()/name(byte[]) 均 NO_IMPLEMENT（vmcid 0x0/minor 0）。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；抽象类经 ORB 工厂或匿名子类通道；接口/桩经反射方法面对照 javap。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-a，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 IDL→Java 映射规范

## 测试用例设计 + 易错细节
1. 形态断言：类/接口/抽象/final/继承面（javap 对照，方法名集合精确相等）。
2. 行为断言：构造回读、字段同引用、默认值表（全部先实测后写入）。
3. 负例断言：精确异常类型（见各测试）。
4. 易错点：
   - TCKind/DefinitionKind 等非 Java enum，无 values()，需反射枚举常量。
   - org.omg.CORBA.Bounds ≠ org.omg.CORBA.TypeCodePackage.Bounds。
   - import org.omg.CORBA.* 会使 java.lang.Object 歧义，测试一律 FQCN。
   - jdk8u372 的 SystemException 默认 completed 是 COMPLETED_NO（非 MAYBE）。
   - IDL 枚举类 toString 为默认 Object 形式，不含常量名。
