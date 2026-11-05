# java.lang.classfile.PseudoInstruction — 设计案

## 1. API 功能
`java.lang.classfile.PseudoInstruction`（空标记接口，extends `CodeElement`）。六族：`LabelTarget`（label()）、`LineNumber`（line()）、`LocalVariable`（slot/name/type/typeSymbol/startScope/endScope）、`LocalVariableType`（slot/name/signature/signatureSymbol/startScope/endScope）、`ExceptionCatch`（catchType Optional/handler/tryStart/tryEnd）、`CharacterRange`（characterRangeStart/characterRangeEnd/flags/startScope/endScope + FLAG_* 常量）。**核心契约：PseudoInstruction instanceof Instruction == false（分类互斥）**。

## 2. Java 用户如何使用
`CodeBuilder` 默认方法发射：`labelBinding(l)`→LabelTarget、`lineNumber(n)`→LineNumber、`localVariable(slot,name,desc,start,end)`→LocalVariable、`localVariableType(slot,name,sig,start,end)`→LocalVariableType、`characterRange(s,e,a,b,flags)`→CharacterRange、`exceptionCatch(...)`/`exceptionCatchAll(...)`→ExceptionCatch。伪指令不占字节码空间，但驱动 LVT/LVTT/LNT/CRT 属性生成。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.instruction.*` 六接口。
- OpenJDK `test/jdk/jdk/classfile/`（LocalVariableTest/LineNumberTest/TestPseudoInstructions 形态）。
- 本机实测：六族全可发射并回读；伪指令与 LVT/LVTT/LNT/CRT 属性成对；`LineNumber` 的 pc 必须落在真实指令上（defineClass 否则 ClassFormatError "Invalid pc in LineNumberTable"）；javac 默认（无 -g:vars）有 LNT 无 LVT 伪指令；`LocalVariable.of(..., null, null)` → NPE。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：LineNumber.of/NopInstruction.of 分类各一）。
- `testFullFamilyEmission`（深化）：一次发射六族 → 回读逐元素断言 **PseudoInstruction ⇒ NOT Instruction**（运行时 `Instruction.class.isInstance` 通道）+ 分类精确性（instanceof Pseudo 当且仅当属六族）；各族计数（lt/lv/lvt/ln/ec/cr）；六族各自 NOT Instruction 断言；全族 IS CodeElement；verify 干净。
- `testAccessorsAndAttributes`（深化）：逐族访问器精确值 —— LocalVariable(slot 0/name arg/type I/typeSymbol I/scope labels 非空)、LocalVariableType(slot/name/signature I/signatureSymbol)、LineNumber(42)、CharacterRange(100/200/FLAG_STATEMENT)、ExceptionCatch(typed catchType==java/lang/IllegalStateException、tryStart bci 0、handler bci 5)、LabelTarget(label 非空)；**伪指令↔属性成对**（LVT/LVTT/LNT/CRT 四属性全出现；LNT 行号 42 且 startPc>=0）。
- `testRealClassAndFactories`（深化）：**真类（javac 无 -g）无 LocalVariable 伪指令、有 LineNumber 伪指令**（实测默认行为）；LineNumber.of 工厂；CharacterRange.FLAG_* 九常量精确值（1/2/4/8/16/32/64/128/256）；LocalVariable.of(null labels) NPE。
- `testDefineClassLoop`（深化）：DynPseudoInstruction+seq（带 LV+LN 伪指令）→ 伪指令计数 → defineClass → go(5)==5 运行闭环（LNT pc 落在真实指令上，否则 ClassFormatError）。
- 易错点：**LineNumberTable 的 pc 指向死区（≥codeLength）时 defineClass 抛 ClassFormatError**——发射 lineNumber 要在活指令处；javac 默认 -g:source,lines（无 vars）。
