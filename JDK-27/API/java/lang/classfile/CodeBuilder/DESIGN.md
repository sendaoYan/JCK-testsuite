# java.lang.classfile.CodeBuilder — 设计案

## 1. API 功能
`java.lang.classfile.CodeBuilder`（interface）。工厂/静态方法：(none)。查询/实例方法：newLabel, startLabel, endLabel, receiverSlot, parameterSlot, allocateLocal, transforming, block, ifThen, ifThenElse。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `CodeBuilderTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.CodeBuilder` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`CodeBuilderTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.CodeBuilder")` + public/interface/继承 + 关键成员。黑盒（`CodeBuilderTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 4.1 深化（契约覆盖版，163 断言，defineClass 运行闭环为主轴）

- `testConstantFamily()`：常量装载指令精确 opcode 表——iconst_m1..iconst_5 / bipush / sipush / aconst_null / LDC(String→REFERENCE StringEntry, size 2) / LDC2_W(long, size 3) / loadConstant 族；逐条 sizeInBytes 断言。
- `testArithmeticConversion()`：imul/idiv/lsub/fdiv/i2l/l2i/i2b/d2i 全部 defineClass 后运行断言（42/42/6/0.25/3L/5/44/2）+ CONVERT 指令 kind。
- `testBranchFamily()`：if_icmpge size3 + target Label；goto 循环 until(5) 终止回 5；LabelTarget 伪指令计数。
- `testArrayFamily()`：int[] 求和循环 iaload==30；anewarray+aastore 字符串数组取 "z"；multianewarray [[I dims=2 ma[1][2]==42；newarray BOOLEAN/INT/LONG 的 newarrayCode 4/10/11 + iastore/iaload 77。
- `testFieldAccessFamily()`：putstatic/getstatic 运行值 3（DynFA1）；Point getfield x==3 / putfield x==9；re-parse 后三元组精确串 "PUTSTATIC:DynFA1.C:I"、"GETFIELD:java/awt/Point.x:I"；field() 返回 FieldRefEntry。
- `testInvokeFamily()`：invokeinterface（List.size，isInterface==true，InterfaceMethodRefEntry）/ invokevirtual（String.concat，运行 "v9"）/ invokestatic（Math.abs(-8)==8）/ invokespecial（<init>，MethodRefEntry）；cat(9)=="v9"、size==3。
- `testReturnStackMonitorFamily()`：5 种返回族 size1；swap [ICONST_1,ICONST_2,SWAP,ISUB,IRETURN]→1；dup+pop→3；monitorenter/monitorexit 序列；checkcast/instanceof false；athrow ISE 传播；monitorenter 栈下溢 IAE 负例。
- `testSwitchFamily()`：tableswitch lowValue=0 highValue=1 cases=2 size=23 + default；lookupswitch [1,5] 升序；运行 tbl(0)==10/tbl(1)==20/tbl(9)==-1/lku(5)==5/lku(2)==0。注：方法名是 lowValue()/highValue()（非 low/high）——实测锁定。
- `testExceptionFamily()`：exceptionCatch(Label,Label,Label,catchType) 四参版与 exceptionCatchAll/Optional.empty 三参版；handler 均为 PseudoInstruction 且 instanceof CodeElement；trying 除零捕获 42。
- `testWideAndPseudo()`：ISTORE_W/ILOAD_W/LSTORE_W/LLOAD_W size4、IINC_W size6、iinc(256,32767) 运行 32767；labelBinding(startLabel()) 抛 IAE "already-set label"（startLabel/endLabel 预绑定）；LVT/LNT 属性生成 + LocalVariable slot/name/typeSymbol/startScope/endScope（注意：LocalVariable 伪指令用 scope 访问器，startPc/length 在 LocalVariableInfo 属性上——实测锁定）；LineNumber.line()==42；newLabel 唯一；receiverSlot==0 / parameterSlot(0)==1 / parameterSlot(2)==4（long 占双 slot）/ allocateLocal(INT) 顺序分配。
- `testDefineClassFib()`：DynFib fib(0..20)==0/1/55/6765 + maxLocals 5 + codeLength 29 精确断言。

实测锁定契约事实：
1. build 内建栈校验：操作数栈下溢在 build 期抛 IAE，消息形如 "Operand stack underflow at bytecode offset N of method ...()"。
2. 不可达代码不报错，builder 以 NOP+ATHROW 序列填充（re-parse 可见精确序列）。
3. startLabel()/endLabel() 返回的标签在进入 CodeBuilder 时已预绑定，对其 labelBinding 抛 IAE 且消息含 "already-set label"；作用域类伪指令（localVariable/LineNumber）直接消费这两个标签。
4. iconst 仅 -1..5；iconst_6+ 不存在，越界值由 bipush(−128..127)/sipush(−32768..32767) 承接；LDC int/float/String 为 size2，long/double 走 LDC2_W size3。
5. wide 前缀自动应用：slot≥256 的 load/store 自动 ILOAD_W 族（size4），iinc 增量超 i16 自动 IINC_W（size6）。
6. tableswitch/lookupswitch 的 pad 对齐计入 sizeInBytes（tableswitch low=0 high=1 实测 23 字节）。
