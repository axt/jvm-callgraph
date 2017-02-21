package com.axt.jvmcallgraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import com.google.common.collect.ImmutableList;

public class CallGraph {
	
	private CallGraphRequest callGraphRequest;
	
	private List<CallGraphNode> rootNodes;
	private Set<MethodInfo> activeMethods;
	private HashMap<MethodInfo, CallGraphNode> methodToNodeMap;
	
	private int depth;
	boolean built = false;
	
	public CallGraph(CallGraphRequest callGraphRequest) {
		this.callGraphRequest = callGraphRequest;
	}

	public void build(int deep) throws IOException {
		this.depth = deep;
		this.activeMethods = new HashSet<>(collectTargetMethods());
		this.rootNodes = setRootNodes(this.activeMethods);
		this.methodToNodeMap = new HashMap<>();
		for(CallGraphNode node: rootNodes) {
			this.methodToNodeMap.put(node.method, node);
		}
		for (int i=0; i < deep; i++) {
			boolean workleft = nextLevel(i==0);
			if(!workleft) break;
		}
		pruneGraph();
		built = true;
	}

	public CallGraphNode getNode(MethodInfo method) {
		checkIsBuilt();
		return methodToNodeMap.get(method);
	}
	
	public Collection<CallGraphNode> getRootNodes() {
		checkIsBuilt();
		return Collections.unmodifiableList(rootNodes);
	}
	
	public int getDepth() {
		return depth;
	}
	
	private void checkIsBuilt() {
		if (!built)
			throw new IllegalStateException("build() should be called before accessing query functions");
	}

	private List<MethodInfo> collectTargetMethods() throws IOException {
		MethodCollector targetMethodCollector = new MethodCollector(callGraphRequest.getTargetMethods());
		for(ClassReader cr : callGraphRequest.getClassReaders()) {
			targetMethodCollector.setClassName(cr.getClassName());
			cr.accept(targetMethodCollector, 0);
		}
		return targetMethodCollector.getCollectedMethods();
	}
	
	private List<CallGraphNode> setRootNodes(Collection<MethodInfo> targetMethods) {
		List<CallGraphNode> rootNodes = new ArrayList<>();
		for(MethodInfo method : targetMethods) {
			rootNodes.add(new CallGraphNode(method));
		}
		return rootNodes;
	}
	
	private boolean nextLevel(boolean top) throws IOException {
		MethodCallCollector methodCallCollector = new MethodCallCollector(activeMethods);
		for(ClassReader cr : callGraphRequest.getClassReaders()) {
			methodCallCollector.setClassName(cr.getClassName());
			cr.accept(methodCallCollector, 0);
		}	
		
		Set<MethodInfo> remainingMethods = new HashSet<>();
		
		for(MethodInfo method: activeMethods) {
			Collection<MethodInfo> callees = methodCallCollector.getCallees(method);
			Predicate<MethodInfo> stopCondition = callGraphRequest.getStopCondition();
			if (!top && stopCondition != null &&  stopCondition.test(method)) {
				continue;
			}
			CallGraphNode methodNode = methodToNodeMap.get(method);
			for (MethodInfo callee : callees) {
				CallGraphNode calleeNode = null;
				
				if (!methodToNodeMap.containsKey(callee)) {
					calleeNode = new CallGraphNode(callee);
					methodToNodeMap.put(callee, calleeNode);
					remainingMethods.add(callee);
					
				} else {
					calleeNode = methodToNodeMap.get(callee);
				}
				methodNode.addCallee(calleeNode);

				if (callGraphRequest.isFollowInterfaces() || callGraphRequest.isFollowSuper()) {
					Set<Predicate<MethodInfo>> scope = this.callGraphRequest.toPredicates(ImmutableList.of(new CallGraphRequest.ExplicitTargetMethod(callee, callGraphRequest.isFollowSuper(), callGraphRequest.isFollowInterfaces())));
					
					MethodCollector targetMethodCollector = new MethodCollector(new ArrayList(scope));
					for(ClassReader cr : callGraphRequest.getClassReaders()) {
						targetMethodCollector.setClassName(cr.getClassName());
						cr.accept(targetMethodCollector, 0);
					}
					
					for(MethodInfo callee2 : targetMethodCollector.getCollectedMethods()) {
						if (callee2 == callee)
							continue;
						if (!methodToNodeMap.containsKey(callee2)) {
							calleeNode = new CallGraphNode(callee2);
							methodToNodeMap.put(callee2, calleeNode);
							remainingMethods.add(callee2);						
						} else {
							calleeNode = methodToNodeMap.get(callee2);
						}
						methodNode.addCallee(calleeNode);
					}
				}
			}
		}
	
		this.activeMethods = remainingMethods;
		return activeMethods.size() > 0;
	}
	
	boolean pruneCalleeNodes(CallGraphNode node, Set<CallGraphNode> visited, int level) {
		if (node.calleeNodes.size() == 0 || level == depth+1) {
			if(!(callGraphRequest.getStopCondition() != null && callGraphRequest.getStopCondition().test(node.method)))
				return false;
			return true;
		} else {
			boolean keepNode = false;
			Iterator<CallGraphNode> it = node.calleeNodes.iterator();
			while(it.hasNext()) {
				Set<CallGraphNode> visitedCurrent = new HashSet<CallGraphNode>(visited);
				visitedCurrent.add(node);
				CallGraphNode callee = it.next();
				if (visitedCurrent.contains(callee)) {
					it.remove();
					continue;
				};
				boolean keepCallee = pruneCalleeNodes(callee, visitedCurrent, level+1);
				if (keepCallee) {
					keepNode |= keepCallee;
				} else {
					it.remove();
				}
			}
			return keepNode;
		}
	}
	
	private void pruneGraph() {
		if (this.callGraphRequest.getPrune()) {
			Iterator<CallGraphNode> it = rootNodes.iterator();
			while(it.hasNext()) {
				if(!pruneCalleeNodes(it.next(), new HashSet<>(), 0)) {
					it.remove();
				}
			}
		}
	}

	private static void printCallGraph(StringBuilder sb, CallGraphNode node, int maxDepth) {
		printNode(sb, node, 0, maxDepth);
	}
	
	private static void printNode(StringBuilder sb, CallGraphNode node, int level, int maxDepth) {
		if (level > maxDepth) {
			return;
		}
		for(int i=0; i < level; i++) {
			sb.append("\t");
		}
		sb.append("(");
		sb.append(level);
		sb.append(")");
		sb.append(node.method);
		sb.append("\n");
		for (CallGraphNode calleeNode : node.calleeNodes) {
			//if (!calleeNode.getVirtual() || calleeNode.size() > 0)
			printNode(sb, calleeNode, level+1, maxDepth);
		}
	}

	public String dump() {
		return dump(getDepth());
	}
	
	public String dump(int maxDepth) {
		StringBuilder sb = new StringBuilder();
		Collection<CallGraphNode> rootNodes = this.getRootNodes();
		for (CallGraphNode rootNode : rootNodes) {
			printCallGraph(sb, rootNode, maxDepth);
		}
		return sb.toString();
	}
}
