# org.omg.PortableInterceptor.USER_EXCEPTION 契约设计

## API 功能
reply_status 常量接口：short value。

## 参考来源
- OMG Portable Interceptors 规范；jdk8u372-b07 javap+反射实测（注意：实测值与规范文本不一致处以实测为准）。

## 测试用例设计
- 值精确；单字段；public static final short。
- reply_status 全族跨类互检（同一断言块内五常量联合精确）。

## 易错规范细节
同上：实测 USER_EXCEPTION==2。
