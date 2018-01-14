package fr.frederic.dynamically.load.jar;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class PluginScanner {

	private WatchService watchService;
	private PluginLoader fileLoader;

	private void init() throws IOException {
		watchService = FileSystems.getDefault().newWatchService();

		final Path path = Paths.get(System.getProperty("user.home"));

		path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
	}

	public void pool() throws IOException, InterruptedException, ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		init();
		WatchKey key;
		while ((key = watchService.take()) != null) {

			for (WatchEvent<?> event : key.pollEvents()) {
				System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
				if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
					System.out.println("File added: " + event.context());
					final File newPlugin = new File(
							System.getProperty("user.home") + File.separator + event.context());
					fileLoader = new PluginLoader();
					fileLoader.addFile(newPlugin);
				}

			}
			key.reset();
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		final PluginScanner directoryWatcherExample = new PluginScanner();
		directoryWatcherExample.pool();
	}
}
