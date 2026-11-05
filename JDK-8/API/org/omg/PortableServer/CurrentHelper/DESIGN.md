# org.omg.PortableServer.CurrentHelper 契约覆盖设计（仅 JDK-8）

## API 功能
objref Helper（全静态）：id/type/insert/extract/read/write/narrow/unchecked_narrow。

## 使用方式
narrow(org.omg.CORBA.Object) 收窄；Any/CDR 为对象引用通道（需真实引用，null 被模板拒收）。

## 参考来源
- OMG IDL-to-Java Helper 规范；jdk8u372-b07 实测（/tmp/omg-proto-e）

## 测试用例设计 + 易错细节
1. id() 精确 `IDL:omg.org/PortableServer/Current:2.3` （Current 也是 2.3 版 repo id）；
   type().kind()==tk_objref、type().name()==Current、type().id()==id()。
2. narrow(null) == null；**老映射缺口：CurrentHelper 没有 unchecked_narrow**（反射断言 NoSuchMethod）。
3. **负例矩阵全 MARSHAL minor==0**：read(空流)/extract(空 Any)/write(null)/insert(any,null)。
   易错：Helper.write 拒 null，而裸 `OutputStream.write_Object(null)` 合法（模板内 instanceof 检查所致）。
