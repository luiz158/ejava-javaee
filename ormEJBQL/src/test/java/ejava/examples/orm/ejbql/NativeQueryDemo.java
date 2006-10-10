package ejava.examples.orm.ejbql;

import java.util.List;

import javax.persistence.Query;

import ejava.examples.orm.ejbql.annotated.Customer;

public class NativeQueryDemo extends DemoBase {

    public void testScalarNativeQuery() {
        log.info("testSimpleNativeQuery");
        
        Query query = em.createNativeQuery(
                "select * from ORMQL_CUSTOMER " +
                "where ORMQL_CUSTOMER.firstName = :first");
        List results = query.setParameter("first", "thing").getResultList();
        assertTrue("no customers found", results.size() > 0);
        for(Object o: results) {
            StringBuilder text = new StringBuilder();
            if (o instanceof Object[]) {
                Object[] objects = (Object[])o;
                for(Object obj: objects) {
                    text.append(obj.toString() + ",");
                }
            }
            else {
                text.append(o.toString());
            }
            log.info("results=" + text);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testEntityNativeQuery() {
        log.info("testEntityNativeQuery");
        
        Query query = em.createNativeQuery(
                "select * from ORMQL_CUSTOMER " +
                "where ORMQL_CUSTOMER.firstName = :first", Customer.class);
        List<Customer> results = query.setParameter("first", "thing")
                                      .getResultList();
        assertTrue("no customers found", results.size() > 0);
        for(Customer c: results) {
            log.info("customer found:" + c);
        }
    }
}
