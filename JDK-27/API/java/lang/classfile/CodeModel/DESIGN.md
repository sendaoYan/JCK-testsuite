# java.lang.classfile.CodeModel — 设计案

## 1. API 功能
`java.lang.classfile.CodeModel`（interface，extends `CompoundElement<CodeElement>` + `AttributedElement` + **`MethodElement`**；运行时 cast 到 `attribute.CodeAttribute` 得 `maxStack()/maxLocals()/codeLength()/codeArray()/labelToBci(Label)`）。实例方法：`parent()`（Optional&lt;MethodModel&gt;）、`exceptionHandlers()`（List&lt;ExceptionCatch&gt;）。

## 2. Java 用户如何使用
`methodModel.code().get()` 取得；`exceptionHandlers()` 给异常表（**与 elementList 中的 ExceptionCatch 是语义等价的不同包装对象**——bci 相同而非同一实例）；for-each 遍历 CodeElement（指令+伪指令）；`labelToBci` 需先 cast 到 CodeAttribute。发射 try-catch 用顶层 `cob.exceptionCatch(start,end,handler,ClassDesc)` / `exceptionCatchAll(...)` 配 `labelBinding`，handler 段不能被 fall-through 直达（否则栈形状冲突 IAE）。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.CodeModel`、`javap java.lang.classfile.attribute.CodeAttribute`。
- OpenJDK `test/jdk/jdk/classfile/`（ShortJumpsFixTest、CodeAttrTest）。
- 本机实测：tryEnd 为**排他** bci、sum(Instruction.sizeInBytes)==codeLength、静态 ()V 无局部变量方法 maxLocals==0、(II)I maxLocals==2/maxStack==2。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：4 指令计数、parent、exceptionHandlers 空、CodeAttribute cast）。
- `testTryCatchReadback`（深化）：发射 try-catch(类型+any)+goto 跳过 handler+局部变量+行号 → 回读 —— exceptionHandlers 2 条（typed catchType==java/lang/Exception、any 为 empty）；tryStart bci==0、tryEnd==5（**排他**）、handler==5；elementList 计数矩阵（6 指令 / 3 LabelTarget（同 bci 合并）/ 2 ExceptionCatch / 1 LocalVariable / 1 LineNumber）；`sum(Instruction.sizeInBytes)==codeLength==codeArray().length` 不变量；codeArray[0]==ICONST_1 bytecode；LVT/LNT 属性值与伪指令一致（LVT name lv/slot 0/type I；LNT line 7）；handlers() 与 elementList 的 EC bci 等价（**非同一对象**，实测）；CodeModel instanceof MethodElement 且 **NOT ClassElement**。
- `testSelfCode`（深化）：真类 main 的 code（codeLength>10、maxStack/maxLocals>=1、RETURN 计数、无 handler）；abstract 方法经 build 验证 code() empty + elementList [AccessFlags]。
- `testDefineClassLoop`（深化）：DynCodeModel+seq div(II)I → maxLocals==2/maxStack==2 → defineClass → div(84,2)==42。
- 易错点：**静态 ()V 无真实局部变量时 maxLocals()==0**（发射 localVariable 伪指令不产生局部槽）；tryEnd 是排他端点；分支方法 build 自动生成 StackMapTable（在 attributes() 而非 elementList）。
