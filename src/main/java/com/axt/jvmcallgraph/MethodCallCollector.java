package com.axt.jvmcallgraph;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class MethodCallCollector extends ClassVisitor {

	private String className;
	private Set<MethodInfo> calledMethodInfos;
	private Set<MethodInfo> calleeMethodInfos = new HashSet<MethodInfo>();

	public MethodCallCollector(Set<MethodInfo> methodInfos) {
		super(Opcodes.ASM5);
		this.calledMethodInfos = methodInfos;
	}

	@Override
	public MethodVisitor visitMethod(int access, final String outerName, final String outerDesc, String signature, String[] exceptions) {

		return new MethodVisitor(Opcodes.ASM5) {
			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				MethodInfo mi = new MethodInfo(owner, name, desc, 0);
				if (calledMethodInfos.contains(mi)) {
					MethodInfo outerMethodInfo = new MethodInfo(className, outerName, outerDesc, access);
					calleeMethodInfos.add(outerMethodInfo);					
//				    int INVOKEVIRTUAL = 182; // visitMethodInsn
//				    int INVOKESPECIAL = 183; // -
//				    int INVOKESTATIC = 184; // -
//				    int INVOKEINTERFACE = 185; // -
//				    int INVOKEDYNAMIC = 186; // visitInvokeDynamicInsn
					System.out.println("CALL " + outerMethodInfo);
				}
			}
		};
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
	public Set<MethodInfo> getCalleeMethodInfos() {
		return calleeMethodInfos;
	}
}