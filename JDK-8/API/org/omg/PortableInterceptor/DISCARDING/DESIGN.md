# org.omg.PortableInterceptor.DISCARDING 契约设计

## API 功能
adapter 状态（POAManager 状态复用）常量接口：short value。

## 参考来源
- OMG Portable Interceptors 规范；jdk8u372-b07 javap+反射实测（注意：实测值与规范文本不一致处以实测为准）。

## 测试用例设计
- 值精确；单字段；public static final short。
- adapter 状态（POAManager 状态复用）全族跨类互检（同一断言块内五常量联合精确）。

## 易错规范细节
五状态 0..4 连续互异；AdapterStateHelper id 精确。
