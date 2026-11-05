# org.omg.PortableInterceptor.IORInterceptor_3_0Holder 契约设计

## API 功能
objref Holder（Streamable）。

## 参考来源
- jdk8u372-b07 实测。

## 测试用例设计
- final/Streamable/默认 null/ctor 存 null/_type()==tk_objref 且 equal(Helper.type())。
