package cn.keking.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.springframework.stereotype.Component;

import com.sun.star.document.UpdateDocMode;

import cn.keking.extend.ControlDocumentFormatRegistry;

/**
 * 创建文件转换器
 *
 * @author yudian-it
 * @date 2017/11/13
 */
@Component
public class ConverterUtils {

	String officeHome;
	// OpenOfficeConnection connection;
	OfficeManager officeManager;

	@PostConstruct
	public void initOfficeManager() throws IOException {
		//// connection = new SocketOpenOfficeConnection(host,8100);
		//// connection.connect();
		DefaultOfficeManagerConfiguration configuration = new DefaultOfficeManagerConfiguration();
		File file = null;
		URL res = ConverterUtils.class.getClassLoader().getResource("OpenOfficePortable/Bin/OpenOffice 4/");
		if (res.toString().startsWith("jar:")) {
			copyJarResources((JarURLConnection) res.openConnection());
			file = new File(System.getProperty("java.io.tmpdir") + "\\OpenOfficePortable\\Bin\\OpenOffice 4");
		} else {
			file = new File(res.getFile());
		}
		// new File(this.getClass().getResource("/").getPath());
		officeHome = file.getAbsolutePath();
		officeHome = java.net.URLDecoder.decode(officeHome, "utf-8");
		// officeHome = file.getAbsolutePath() +
		// "\\OpenOfficePortable\\Bin\\OpenOffice 4";
		configuration.setOfficeHome(officeHome);
		configuration.setPortNumber(8100);
		officeManager = configuration.buildOfficeManager();
		officeManager.start();
		// 设置任务执行超时为5分钟
		// configuration.setTaskExecutionTimeout(1000 * 60 * 5L);//
		// 设置任务队列超时为24小时
		// configuration.setTaskQueueTimeout(1000 * 60 * 60 * 24L);//
	}

	public void loadRecourseFromJarByFolder(String folderPath) throws IOException {
		URL url = getClass().getResource(folderPath);
		URLConnection urlConnection = url.openConnection();
		copyJarResources((JarURLConnection) urlConnection);
	}

	private void copyJarResources(JarURLConnection jarURLConnection) throws IOException {
		JarFile jarFile = jarURLConnection.getJarFile();
		Enumeration<JarEntry> entrys = jarFile.entries();
		while (entrys.hasMoreElements()) {
			JarEntry entry = entrys.nextElement();
			if (entry.getName().startsWith(jarURLConnection.getEntryName()) && !entry.getName().endsWith("/")) {
				loadRecourseFromJar("/" + entry.getName());
			}
		}
		jarFile.close();
	}

	public void loadRecourseFromJar(String path) throws IOException {
		if (!path.startsWith("/")) {
			throw new IllegalArgumentException("The path has to be absolute (start with '/').");
		}

		if (path.endsWith("/")) {
			throw new IllegalArgumentException("The path has to be absolute (cat not end with '/').");
		}

		int index = path.lastIndexOf('/');
		String filename = path.substring(index + 1);
		String folderPath = path.substring(0, index + 1);

		// If the folder does not exist yet, it will be created. If the folder
		// exists already, it will be ignored
		File dir = new File(System.getProperty("java.io.tmpdir") + folderPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// If the file does not exist yet, it will be created. If the file
		// exists already, it will be ignored
		filename = System.getProperty("java.io.tmpdir") + path;
		File file = new File(filename);

//		if (!file.exists() && !file.createNewFile()) {
//			return;
//		}
		if (file.exists()) {
			return;
		}

		// Prepare buffer for data copying
		byte[] buffer = new byte[1024];
		int readBytes;

		// Open and check input stream
		URL url = getClass().getResource(path);
		URLConnection urlConnection = url.openConnection();
		InputStream is = urlConnection.getInputStream();

		if (is == null) {
			throw new FileNotFoundException("File " + path + " was not found inside JAR.");
		}

		// Open output stream and copy data between source file in JAR and the
		// temporary file
		OutputStream os = new FileOutputStream(file);
		try {
			while ((readBytes = is.read(buffer)) != -1) {
				os.write(buffer, 0, readBytes);
			}
		} finally {
			// If read/write fails, close streams safely before throwing an
			// exception
			os.close();
			is.close();
		}

	}

	public OfficeDocumentConverter getDocumentConverter() {
		OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager,
				new ControlDocumentFormatRegistry());
		converter.setDefaultLoadProperties(getLoadProperties());
		return converter;
	}

	private Map<String, ?> getLoadProperties() {
		Map<String, Object> loadProperties = new HashMap<>(10);
		loadProperties.put("Hidden", true);
		loadProperties.put("ReadOnly", true);
		loadProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);
		loadProperties.put("CharacterSet", Charset.forName("UTF-8").name());
		return loadProperties;
	}

	@PreDestroy
	public void destroyOfficeManager() {
		if (null != officeManager && officeManager.isRunning()) {
			officeManager.stop();
		}
	}

}
