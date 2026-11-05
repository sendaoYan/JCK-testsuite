# org.omg.IOP.TAG_INTERNET_IOP 契约设计

## API 功能
IDL ProfileId 常量接口：`int value = 0`（IIOP profile 的 tag）。单字段常量接口，无方法。

## 使用方式
`new TaggedProfile(TAG_INTERNET_IOP.value, data)`；配合 `ProfileIdHelper` 走 Any/CDR 通道。

## 参考来源
- OMG IOP 规范（ProfileId: TAG_INTERNET_IOP=0, TAG_MULTIPLE_COMPONENTS=1）
- jdk8u372-b07 rt.jar javap + 反射实测（2026-11-05）

## 测试用例设计
- 接口形态：isInterface、单字段、public static final、int 类型。
- 值精确：==0；与 TAG_MULTIPLE_COMPONENTS(1) 互异；保留区连续（0,1）。
- 跨通道：ProfileIdHelper.id() 精确串 "IDL:omg.org/IOP/ProfileId:1.0"。

## 易错规范细节
- ComponentId 命名空间与 ProfileId 独立：TAG_ORB_TYPE==0 与本常量数值相同但语义不同，勿断言互异。
- IDL unsigned long 映射 Java int（非 long）。
