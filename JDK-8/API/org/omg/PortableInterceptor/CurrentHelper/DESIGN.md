# org.omg.PortableInterceptor.CurrentHelper 契约设计

## API 功能
objref Helper（含 narrow/unchecked_narrow）。

## 参考来源
- jdk8u372-b07 实测。

## 测试用例设计
- id 精确；type tk_objref；narrow(null)/unchecked_narrow(null)==null；extract(空 Any)→MARSHAL。
