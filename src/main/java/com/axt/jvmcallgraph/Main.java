package com.axt.jvmcallgraph;

import java.io.IOException;
import java.util.HashSet;

import org.objectweb.asm.ClassReader;

class Main {
	static final String RTPATH = "../../Java/JDK1.8.0_73/rt";
	
	
	
	public static void main(String[] args) throws IOException {

		CallGraphRequest callGraphRequest = new CallGraphRequest.Builder()
			.addDirectorySource(RTPATH)
			.addTargetMethod(x -> x.isNative() && x.getName().equals("XFillPoly"))
			.build();
	
		MethodCollector targetMethodCollector = new MethodCollector(callGraphRequest.getTargetMethods());
		
		for(ClassReader cr : callGraphRequest.getClassReaders()) {
			targetMethodCollector.setClassName(cr.getClassName());
			cr.accept(targetMethodCollector, 0);
		}
		
		for(MethodInfo mi : targetMethodCollector.getCollectedMethods()) {
			System.out.println(mi);
		}
		
		MethodCallCollector methodCallCollector = new MethodCallCollector(new HashSet<>(targetMethodCollector.getCollectedMethods()));
		for(ClassReader cr : callGraphRequest.getClassReaders()) {
			methodCallCollector.setClassName(cr.getClassName());
			cr.accept(methodCallCollector, 0);
		}

		
	}
}
