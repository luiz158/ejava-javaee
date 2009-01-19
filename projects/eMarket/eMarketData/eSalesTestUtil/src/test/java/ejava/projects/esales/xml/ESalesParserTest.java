package ejava.projects.esales.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ejava.projects.esales.dto.Account;
import ejava.projects.esales.dto.Address;
import ejava.projects.esales.dto.ESales;

import junit.framework.TestCase;

public class ESalesParserTest extends TestCase {
	private static final Log log = LogFactory.getLog(ESalesParser.class);
	private String inputDir = System.getProperty("inputDir");
	
	public void setUp() {
		assertNotNull("inputDir not supplied", inputDir);
	}
	
	public void testParser() throws Exception {
		File inDir = new File(inputDir);
		File[] files = inDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.startsWith("eSales-") &&
						name.endsWith(".xml"));
			}
		});
		for (File file : files) {
			testParser(file.getCanonicalPath());
		}
	}
	public void testParser(String inputFile) throws Exception {
		log.info("*** testParser:" + inputFile + " ***");
		
		InputStream is = new FileInputStream(inputFile);
		BufferedInputStream bis = new BufferedInputStream(is);
		ESalesParser parser = new ESalesParser(ESales.class, bis);
		Object object=null;
		do {
			object = parser.getObject("account","address","auction","image");
			if (object != null) { dump(object); };
		} while (object != null);
		bis.close();
	}

	private void dump(Object object) throws Exception {
		StringBuilder text = new StringBuilder();
		for (Method m : object.getClass().getDeclaredMethods()) {
			if (m.getName().startsWith("get")) {
				String propertyName =m.getName().substring("get".length());
				Object value = m.invoke(object, new Object[] {});
				text.append(object.getClass().getName() + "." +
						propertyName + "=" +
						value + "\n");
			}
		}
		log.debug(text);
	}

}