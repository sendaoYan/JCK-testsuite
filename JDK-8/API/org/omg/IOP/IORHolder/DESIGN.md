# org.omg.IOP.IORHolder 契约设计

## API 功能
Streamable Holder：value 字段、_read/_write/_type。

## 参考来源
- OMG IDL/Java 映射；jdk8u372-b07 实测。

## 测试用例设计
- final/Streamable/默认 null/带参回读。
- _write→_read CDR 往返（含 profiles 元素逐字节）。
- _type()==tk_struct；同流双 Holder 连续往返。

## 易错规范细节
- IOR 是 struct 而非值类型，_write 走 Helper.write。
