package com.axt.jvmcallgraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.objectweb.asm.ClassReader;
import com.google.common.collect.ImmutableList;

public class CallGraphRequest {

	private final List<BytecodeSource> sources;
	private final List<Predicate<MethodInfo>> targetMethods;
	private final Predicate<MethodInfo> stopCondition;
	private final boolean prune;
	
	private List<ClassReader> cachedClassReaders = null;
	private ClassHierarchy classHierarchy = null;
	private final boolean followInterfaces;
	private final boolean followSuper;

	
	private CallGraphRequest(Builder builder) {
		this.sources = new ArrayList<>(builder.sources);
		Set<Predicate<MethodInfo>> targetMethods = new HashSet<>();
		targetMethods.addAll(builder.targetMethods);
		targetMethods.addAll(toPredicates(builder.explicitTargetMethods));
		this.targetMethods = new ArrayList<>(targetMethods);
		this.stopCondition = builder.stopCondition;
		this.prune = builder.prune;
		this.followInterfaces = builder.followInterfaces;
		this.followSuper = builder.followSuper;
	}

	public Set<Predicate<MethodInfo>> toPredicates(List<ExplicitTargetMethod> explicitTargetMethods) {
		Set<Predicate<MethodInfo>> predicates = new HashSet<>();
		for (ExplicitTargetMethod etm : explicitTargetMethods) {
			List<String> classNames = new ArrayList<>();
			List<String> workList = new ArrayList<>();
			workList.add(etm.methodInfo.getClassName());
			while(!workList.isEmpty()) {
				String className = workList.remove(0);
				classNames.add(className);
				if (etm.withSuper) {
					String superName = getClassHierarchy().getSuper(className);
					if (superName != null) {
						workList.add(superName);
					}
				}
				if (etm.withInterfaces) {
					for (String interfaceName : getClassHierarchy().getInterfaces(className)) {
						workList.add(interfaceName);
					}
				}
			}
			for (String className : classNames) {
				predicates.add(toPredicate(new MethodInfo(className, etm.methodInfo.getName(), etm.methodInfo.getDescription())));
			}
		}
		return predicates;
	}
	
	private Predicate<MethodInfo> toPredicate(final MethodInfo methodInfo) {
		return new Predicate<MethodInfo>() {
			@Override
			public boolean test(MethodInfo t) {
				boolean matches = true;
				matches &= t.getName().equals(methodInfo.getName());
				matches &= t.getClassName().equals(methodInfo.getClassName());
				if (methodInfo.getDescription() != null) {
					matches &= t.getDescription().equals(methodInfo.getDescription());
				}
				return matches;
			}
		};
	}

	public ClassHierarchy getClassHierarchy() {
		if (this.classHierarchy == null) {
			ClassHierarchy classHierarchy = new ClassHierarchy();
			classHierarchy.build(getClassReaders());
			this.classHierarchy = classHierarchy;
		}
		return this.classHierarchy;
	}

	public List<ClassReader> getClassReaders() {
		if (this.cachedClassReaders == null) {
			try {
				List<ClassReader> cachedClassReaders = new ArrayList<>();
				for(BytecodeSource source : sources) {
					cachedClassReaders.addAll(source.getClassReaders());
				}
				this.cachedClassReaders = cachedClassReaders;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return this.cachedClassReaders;
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
	
	public boolean getPrune() {
		return prune;
	}

	public boolean isFollowInterfaces() {
		return followInterfaces;
	}

	public boolean isFollowSuper() {
		return followSuper;
	}



	public final static class ExplicitTargetMethod {
		MethodInfo methodInfo;
		boolean withSuper;
		boolean withInterfaces;
		public ExplicitTargetMethod(MethodInfo methodInfo, boolean withSuper, boolean withInterfaces) {
			super();
			this.methodInfo = methodInfo;
			this.withSuper = withSuper;
			this.withInterfaces = withInterfaces;
		}
	}
	
	public final static class Builder {
		private List<BytecodeSource> sources = new ArrayList<>();
		private final List<Predicate<MethodInfo>> targetMethods = new ArrayList<>();
		private final List<ExplicitTargetMethod> explicitTargetMethods = new ArrayList<>();
		private Predicate<MethodInfo> stopCondition;
		private boolean prune;
		private boolean followSuper;
		private boolean followInterfaces;

		public Builder() {
		}
		
		public Builder addDirectorySource(String directory) {
			sources.add(new DirectoryBytecodeSource(directory));
			return this;
		}

		public Builder addJarSource(String jarfile) {
			sources.add(new JarBytecodeSource(jarfile));
			return this;
		}
		
		public Builder addClasspathSource(ClassLoader classLoader, Collection<String> basePackages) {
			sources.add(new ClasspathBytecodeSource(classLoader, basePackages));
			return this;
		}
		
		public Builder addClasspathSource(ClassLoader classLoader, String basePackage) {
			sources.add(new ClasspathBytecodeSource(classLoader, ImmutableList.of(basePackage)));
			return this;
		}

		public Builder addTargetMethod(Predicate<MethodInfo> predicate) {
			targetMethods.add(predicate);
			return this;
		}
		
		public Builder addExplicitTargetMethod(MethodInfo methodInfo, boolean withSuper, boolean withInterfaces) {
			if (methodInfo.getName() == null)
				throw new IllegalArgumentException("Method name is mandatory");
			if (methodInfo.getClassName() == null)
				throw new IllegalArgumentException("Method class is mandatory");
			explicitTargetMethods.add(new ExplicitTargetMethod(methodInfo, withSuper, withInterfaces));
			return this;
		}

		public Builder stopCondition(Predicate<MethodInfo> stopCondition) {
			this.stopCondition = stopCondition;
			return this;
		}
		
		public Builder prune(boolean prune) {
			this.prune = prune;
			return this;
		}

		public Builder followInterfaces(boolean followInterfaces) {
			this.followInterfaces = followInterfaces;
			return this;
		}

		public Builder followSuper(boolean followSuper) {
			this.followSuper = followSuper;
			return this;
		}

		public CallGraphRequest build() {
			return new CallGraphRequest(this);
		}
	}	
}
