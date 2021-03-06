package ejava.examples.ejbwar.inventory.rs;

import java.io.IOException;


import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class will cause the JTA transaction the determines the scope
 * of how long JPA can communicate with the DB to be extended until
 * after all JAXB marshalling is complete.
 */
public class TxFilter implements Filter {
    private static final Log log = LogFactory.getLog(TxFilter.class);
    @Inject
    private static UserTransaction tx;

    
    public void doFilter(ServletRequest request, 
            ServletResponse response, 
            FilterChain chain) throws IOException, ServletException {
    	
        try {
        	log.debug("beginning user transaction from filter");
        	begin();
        	chain.doFilter(request, response);
		} finally {
    		if (isRollbackOnly()) {
            	log.debug("rolling back user transaction from filter");
    			rollback();
    		}
    		else if (isActive()) {
            	log.debug("committing user transaction from filter");
       			commit();
        	}
        }
    }
    
    public void begin() {
        try {
        	tx.begin();
        } catch (NotSupportedException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (SystemException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} 
    }
    
    public boolean isActive() {
    	try {
			return tx.getStatus()==Status.STATUS_ACTIVE;
		} catch (SystemException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
    }
    
    public boolean isRollbackOnly() {
    	try {
			return tx.getStatus()==Status.STATUS_MARKED_ROLLBACK;
		} catch (SystemException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
    }
    public void commit() {
    	try {
			tx.commit();
		} catch (SecurityException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (IllegalStateException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (RollbackException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (HeuristicMixedException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (HeuristicRollbackException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (SystemException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
    }
    
    public void rollback() {
    	try {
			tx.rollback();
		} catch (IllegalStateException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (SecurityException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (SystemException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
    }

	@Override
	public void destroy() {
	}


	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
}
