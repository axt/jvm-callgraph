package com.axt.jvmcallgraph;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FileArchive;
import org.apache.xbean.finder.archive.JarArchive;
import org.objectweb.asm.ClassReader;

public class ClasspathBytecodeSource implements BytecodeSource {

	private Collection<String> basePackages;
	private ClassLoader classLoader;

	public ClasspathBytecodeSource(ClassLoader classLoader, Collection<String> basePackages) {
		this.classLoader = classLoader;
		this.basePackages = basePackages;
	}

	@Override
	public List<ClassReader> getClassReaders() throws IOException {
		
		
		List<Archive> archives = new ArrayList<>();
		for (String basePackage : basePackages) {
			Enumeration<URL> resources = ClasspathBytecodeSource.class.getClassLoader().getResources(basePackage.replace('.', '/'));
			while(resources.hasMoreElements()) {
				URL url = resources.nextElement();
				if ("zip".equals(url.getProtocol()) || "jar".equals(url.getProtocol())) {
					archives.add(new JarArchive(classLoader, url));
				} else {
					archives.add(new FileArchive(classLoader, url, basePackage));
				}
			}
		}
		
		CompositeArchive compositeArchive = new CompositeArchive(archives);

		return StreamSupport.stream(compositeArchive.spliterator(), false)
				.map(new Function<Archive.Entry, ClassReader>() {
					@Override
					public ClassReader apply(Archive.Entry t) {
						try {
							return new ClassReader(t.getBytecode());
						} catch(IOException e) {
							//TODO
							System.out.println("EXC");
							return null;
						}
					}				
				})
				.filter(x -> x != null)
				.collect(Collectors.toList());
	}
}
