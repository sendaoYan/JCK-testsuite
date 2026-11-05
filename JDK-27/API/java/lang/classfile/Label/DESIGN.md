# java.lang.classfile.Label — 设计案

## 1. API 功能
`java.lang.classfile.Label`（**纯标记接口，零方法**；`Label.getReferrableLabel` 不存在）。实例只能经 `CodeBuilder.newLabel()` 获得；`CodeBuilder.labelBinding(Label)` 绑定到当前写入位置（返回同一 builder，fluent）。实现对象实测同时实现 `LabelTarget`（jdk LabelImpl implements Label, LabelTarget）→ 间接是 CodeElement。

## 2. Java 用户如何使用
`newLabel()` 得标签 → 作为 branch/switch 的 target 或 exceptionCatch 的边界 → `labelBinding` 绑定。回读侧：`Branch.target()`/`LabelTarget.label()`/`CodeAttribute.labelToBci(Label)`。**身份契约（同一模型内）**：解析出的 `BranchInstruction.target()` 与目标处 `LabelTarget.label()` 是同一对象；`labelToBci` 对两者给出同值。build 侧语义：未绑定（被引用但从不 labelBinding）→ IAE（即使 DROP_DEAD_LABELS）；死标签（绑定但无引用）→ build 成功且回读时 LabelTarget 被剔除；同 bci 绑定多个被引用标签 → 回读合并为 1 个 LabelTarget。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.Label`（空）。
- OpenJDK `test/jdk/jdk/classfile/`（ShortJumpsFixTest/TestCodeBuilder）。
- 本机实测：unbound → IAE "Detected branch target out of bytecode range at bytecode offset 1 of method m(int)"（FAIL_ON_DEAD_LABELS 与 DROP_DEAD_LABELS **均抛**）；dead label → LabelTarget 计数 0；双标签同 bci → 1 个 LabelTarget；newLabel() 两次 != ；labelBinding 返回 == 同一 builder。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：labelBinding 后 LabelTarget 出现计数）。
- `testLabelIdentity`（深化）：`newLabel()` 两次 **!=**、自反 equals、互不 equals；`labelBinding` 返回 **同一 CodeBuilder**；Label 零方法标记接口断言；**newLabel() 实例 IS LabelTarget/CodeElement（impl-observable 实测）且 NOT Instruction**。
- `testBindingReadback`（深化）：分支发射 → 回读 **`BranchInstruction.target()==LabelTarget.label()`（同对象身份契约）**；IFEQ sizeInBytes==3；labelToBci 值在 [0,codeLength) 且==6（iload1+ifeq3+iconst1+ireturn1）；branch 与 LabelTarget 的 labelToBci 一致；GOTO 发射+verify。
- `testUnboundAndDeadLabels`（深化）：**未绑定标签**在 FAIL_ON_DEAD_LABELS 与 DROP_DEAD_LABELS 两选项下均 IAE（精确消息前缀）；**死标签** build 成功 + 回读 LabelTarget 计数 0 + verify 干净；**同 bci 双标签合并**（1 个 LabelTarget）+ labelToBci 有效。
- `testDefineClassLoop`（深化）：DynLabel+seq sign(I)I 两路分支 → defineClass → sign(3)==1 / sign(-3)==-1（**两路都真实执行**）。
- 易错点：DeadLabelsOption 管的是「绑定但无引用」，**管不了「引用但未绑定」**（后者总是 IAE）；跨模型标签不可比较（每次 parse 产生新 Label 对象）。
