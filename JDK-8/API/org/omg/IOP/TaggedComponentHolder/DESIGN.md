# org.omg.IOP.TaggedComponentHolder 契约设计

## API 功能
Streamable Holder。默认 null、wire 往返、_type tk_struct。

## 参考来源
- OMG IDL/Java 映射；jdk8u372-b07 实测。

## 测试用例设计
- final/Streamable/默认 null/ctor 回读/_write→_read 往返/_type kind。

## 易错规范细节
- 与 Helper.type() 同源 TypeCode（懒加载缓存，调用次序无关）。
