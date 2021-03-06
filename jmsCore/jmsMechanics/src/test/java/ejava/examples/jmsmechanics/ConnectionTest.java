package ejava.examples.jmsmechanics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class ConnectionTest extends JMSTestBase {
    static Log log = LogFactory.getLog(ConnectionTest.class);

    @Test
    public void testConnectionMetadata() throws Exception {
        log.info("*** testConnectionMetadata ***");
        
        log.info("connection.metaData=" + connection.getMetaData());
        log.info("connection.metaData.JMSMajorVersion=" + 
                connection.getMetaData().getJMSMajorVersion());
        log.info("connection.metaData.JMSMinorVersion=" + 
                connection.getMetaData().getJMSMinorVersion());
        log.info("connection.metaData.JMSProviderName=" + 
                connection.getMetaData().getJMSProviderName());
        log.info("connection.metaData.JMSVersion=" + 
                connection.getMetaData().getJMSVersion());
        log.info("connection.metaData.providerMajorVersion=" + 
                connection.getMetaData().getProviderMajorVersion());
        log.info("connection.metaData.providerMinorVersion=" + 
                connection.getMetaData().getProviderMinorVersion());
        log.info("connection.metaData.providerVersion=" + 
                connection.getMetaData().getProviderVersion());
    }
    
}
