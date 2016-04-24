package com.axt.jvmcallgraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

class MethodCallCollector extends ClassVisitor {

	private String className;
	private Set<MethodInfo> calledMethods;
	private Multimap<MethodInfo, MethodInfo> calleeMethods = HashMultimap.create();

	public MethodCallCollector(Collection<MethodInfo> calledMethods) {
		super(Opcodes.ASM5);
		this.calledMethods = new HashSet<MethodInfo>(calledMethods);
	}

	@Override
	public MethodVisitor visitMethod(int access, final String outerName, final String outerDesc, String signature, String[] exceptions) {
		return new MethodVisitor(Opcodes.ASM5) {
			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				MethodInfo mi = new MethodInfo(owner, name, desc);
				if (calledMethods.contains(mi)) {
					MethodInfo outerMethodInfo = new MethodInfo(className, outerName, outerDesc);
					outerMethodInfo.setAccess(access);
					calleeMethods.put(mi, outerMethodInfo);					
//				    int INVOKEVIRTUAL = 182;
//				    int INVOKESPECIAL = 183;
//				    int INVOKESTATIC = 184; 
//				    int INVOKEINTERFACE = 185;
					//TODO
//				    int INVOKEDYNAMIC = 186; // visitInvokeDynamicInsn
//					System.out.println("CALL " + outerMethodInfo + " -> " + mi + " " + opcode);
				}
			}
		};
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
	Collection<MethodInfo> getCallees(MethodInfo method) {
		return this.calleeMethods.get(method);
	}
}