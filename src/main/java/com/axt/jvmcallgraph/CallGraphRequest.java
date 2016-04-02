package com.axt.jvmcallgraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;

import com.axt.jvmcallgraph.CallGraphRequest.Builder;


public class CallGraphRequest {

	private final List<BytecodeSource> sources;
	private final List<Predicate<MethodInfo>> targetMethods;
	private final Predicate<MethodInfo> stopCondition;
	
	private CallGraphRequest(Builder builder) {
		this.sources = new ArrayList<>(builder.sources);
		this.targetMethods = new ArrayList<>(builder.targetMethods);
		this.stopCondition = builder.stopCondition;
	}
	
	public List<ClassReader> getClassReaders() throws IOException {
		List<ClassReader> ret = new ArrayList<>();
		for(BytecodeSource source : sources) {
			ret.addAll(source.getClassReaders());
		}
		return ret;
	}

	public List<Predicate<MethodInfo>> getTargetMethods() throws IOException {
		List<Predicate<MethodInfo>> ret = new ArrayList<>();
		for(Predicate<MethodInfo> targetMethod: targetMethods) {
			ret.add(targetMethod);
		}
		return ret;
	}

	public Predicate<MethodInfo> getStopCondition() {
		return stopCondition;
	}
	
	public final static class Builder {
		private List<BytecodeSource> sources = new ArrayList<>();
		private final List<Predicate<MethodInfo>> targetMethods = new ArrayList<>();
		private Predicate<MethodInfo> stopCondition;

		public Builder() {
		}
		
		public Builder addDirectorySource(String directory) {
			sources.add(new DirectoryBytecodeSource(directory));
			return this;
		}

		public Builder addTargetMethod(Predicate<MethodInfo> predicate) {
			targetMethods.add(predicate);
			return this;
		}

		public Builder stopCondition(Predicate<MethodInfo> stopCondition) {
			this.stopCondition = stopCondition;
			return this;
		}

		public CallGraphRequest build() {
			return new CallGraphRequest(this);
		}
	}	
}
