package com.axt.jvmcallgraph;

import java.io.IOException;

import org.objectweb.asm.ClassReader;

class Main {
	static final String RTPATH = "../../Java/JDK1.8.0_73/rt";
	
	public static void main(String[] args) throws IOException {

		CallGraphRequest callGraphRequest = new CallGraphRequest.Builder()
			.addDirectorySource(RTPATH)
			.addTargetMethod(x -> x.isNative() && x.getName().equals("copyArea"))
			.build();
	
		TargetMethodCollector targetMethodCollector = new TargetMethodCollector(callGraphRequest.getTargetMethods());
		
		for(ClassReader cr : callGraphRequest.getClassReaders()) {
			cr.accept(targetMethodCollector, 0);
		}
		
		for(MethodInfo mi : targetMethodCollector.getCollectedMethods()) {
			System.out.println(mi);
		}
	}
}
