package com.axt.jvmcallgraph;

import java.io.IOException;
import java.util.Collection;

class Main {
	static final String RTPATH = "../../Java/JDK1.8.0_73/rt";

	public static void main(String[] args) throws IOException {

		CallGraphRequest callGraphRequest = new CallGraphRequest.Builder()
			.addDirectorySource(RTPATH)
			.addTargetMethod(x -> x.isNative())
			.build();
	
		CallGraph callGraph = new CallGraph(callGraphRequest);
		callGraph.build(100);
		
		Collection<CallGraphNode> rootNodes = callGraph.getRootNodes();
		for (CallGraphNode rootNode : rootNodes) {
		}
		
	}
}
