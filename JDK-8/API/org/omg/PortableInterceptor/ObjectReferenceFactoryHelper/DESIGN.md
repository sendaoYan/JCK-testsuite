# ObjectReferenceFactoryHelper 契约设计

## API 功能
**valuetype** Helper（无 narrow 家族）。

## 参考来源
- jdk8u372-b07 实测。

## 测试用例设计
- id 精确；type().kind()==tk_value；无 narrow/unchecked_narrow（反射证）；extract(空 Any)→MARSHAL。

## 易错规范细节
- valuetype 与 objref 的 Helper 面差异：无 narrow。
