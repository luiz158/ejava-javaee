package ejava.examples.javase5;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class GenericsTest {
    private static final Log log = LogFactory.getLog(GenericsTest.class);
    
    private static class TestType {
        public String name;
    }
    
    /**
     * Raw exceptions expose runtime to ClassCastExceptions 
     *
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testRawCollection() {
        log.info("testRawCollection");
        
        TestType anObject = new TestType();
        anObject.name = "test object";
        
		List rawCollection = new ArrayList();
        rawCollection.add(anObject);
        
        //the cast to (TestType) is required here
        TestType rawObject = (TestType)rawCollection.get(0);
        log.info("raw way=" + rawObject.name);                
    }    

    /**
     * Typed collections check at compile time
     *
     */
    @Test
    public void testTypedCollection() {
        log.info("testTypedCollection");
        
        TestType anObject = new TestType();
        anObject.name = "test object";
        
        List<TestType> typedCollection = new ArrayList<TestType>();
        typedCollection.add(anObject);
        
        //no cast necessary
        TestType typedObject = typedCollection.get(0);
        
        log.info("typed way=" + typedObject.name);        
    }    
    
    /**
     * Checked collections check at runtime.
     *
     */
    @SuppressWarnings("unchecked")
    @Test
	public void testCheckedCollection() {
        log.info("testCheckedCollection");
        
        TestType anObject = new TestType();
        anObject.name = "test object";
        
        List<TestType> typedCollection = new ArrayList<TestType>();
        
        //typed collections get checked at compile time
        typedCollection.add(anObject);
        
        //but things can still happen that bypass compiler
        @SuppressWarnings("rawtypes")
		List rawCollection = typedCollection;
        try {
            rawCollection.add(new String("slipped by 2"));
        }
        catch (ClassCastException ex) {
            fail("unexpected catch of raw Collection");
        }
        
        //checked collections verify types at runtime        
        List<TestType> checkedCollection = 
            Collections.checkedList(typedCollection, TestType.class);
        rawCollection = checkedCollection;
        boolean wasCaught = false; 
        try {
            rawCollection.add(new String("caught"));
        }
        catch (ClassCastException ex) {
            log.info("caught you!");
            wasCaught = true;
        }
        assertTrue("checked type not caught", wasCaught);
        
        int exceptions=0;
        for(Iterator<TestType> itr=typedCollection.iterator(); itr.hasNext();) {
            try {
                TestType checkedObject = itr.next();
                log.info("got " + checkedObject);
            }
            catch (ClassCastException ex) {
                log.info("ouch");
                exceptions++;
            }
        }
        assertTrue("unexpected number of exceptions:" + exceptions, 
                exceptions == 1);
    }    
}
