package fr.frederic.dynamically.load.jar;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import fr.frederic.dynamically.load.jar.plugin.PluginAction;

public class PluginLoader extends URLClassLoader {

	private static final String CLASS_PATTERN_NAME = "^((\\w*\\/))+\\w*\\.class$";

	public PluginLoader() {
		this(new URL[] {});
	}

	public PluginLoader(URL[] urls) {
		super(urls);
	}

	public void addFile(File pluginFile)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

		final Set<String> allClassToLoad = new HashSet<>();

		final JarFile jarFile = new JarFile(pluginFile);
		Iterator<JarEntry> iter = jarFile.stream().iterator();
		while (iter.hasNext()) {
			final JarEntry jarEntry = iter.next();
			String name = jarEntry.getName();
			if (name.matches(CLASS_PATTERN_NAME)) {
				allClassToLoad.add(name.replaceAll("/", ".").substring(0, name.length() - ".class".length()));
			}
		}
		addURL(pluginFile.toURI().toURL());
		for (String classToLoad : allClassToLoad) {
			Class loadedClass = this.loadClass(classToLoad);
			if (isAPlugin(loadedClass)) {
				PluginAction pluginAction = (PluginAction) loadedClass.newInstance();
				System.out.println("\n"//
						+ "\n"//
						+ "\n"//
						+ "******************\n"//
						+ "*  PLUGIN RESULT *\n"//
						+ "******************\n");

				pluginAction.execute();
				
				System.out.println("\n"//
						+ "\n"//
						+ "\n"//
						+ "******************\n");

			}
		}
		jarFile.close();
		delockFile();
	}

	private boolean isAPlugin(Class clazz) {
		return Stream.of(clazz.getInterfaces()).filter(clzz -> clzz == PluginAction.class).findAny().isPresent();
	}

	/**
	 * Closes all open jar files.. Windows empêche la suppression des fichiers
	 * https://stackoverflow.com/questions/3216780/problem-reloading-a-jar-using-urlclassloader
	 */
	public void delockFile() {
		try {
			Class clazz = java.net.URLClassLoader.class;
			Field ucp = clazz.getDeclaredField("ucp");
			ucp.setAccessible(true);
			Object sunMiscURLClassPath = ucp.get(this);
			Field loaders = sunMiscURLClassPath.getClass().getDeclaredField("loaders");
			loaders.setAccessible(true);
			Object collection = loaders.get(sunMiscURLClassPath);
			for (Object sunMiscURLClassPathJarLoader : ((Collection) collection).toArray()) {
				try {
					Field loader = sunMiscURLClassPathJarLoader.getClass().getDeclaredField("jar");
					loader.setAccessible(true);
					Object jarFile = loader.get(sunMiscURLClassPathJarLoader);
					((JarFile) jarFile).close();
				} catch (Throwable t) {
					// if we got this far, this is probably not a JAR loader so skip it
				}
			}
		} catch (Throwable t) {
			// probably not a SUN VM
		}
	}

}
