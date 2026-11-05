# org.omg.CosNaming.NameComponentHolder

## API 功能
NameComponent 的 out/inout 参数 Holder（Streamable）。
## 使用方式
默认构造 value==null；带参构造；_read/_write 过 CDR；_type() 为 tk_struct。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- final + Streamable；默认 null；ctor 引用原样；_type().kind()==tk_struct。
- _write→_read 往返逐字段；holder._read 消费 Helper.write 写出的流（两套写入源等价）。
