package ejava.examples.jndidemo.ejb;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import ejava.examples.jndidemo.JNDIHelper;

@Stateful(name="BakeScheduler")
@EJBs({
    @EJB(name="ejb/cook", beanInterface=CookLocal.class, beanName="CookEJB")
})
public class BakeSchedulerEJB 
    extends SchedulerBase implements BakeSchedulerRemote {

    public String getName() { return "BakeSchedulerEJB"; }
    
    @PersistenceContext(unitName="jndidemo",
                        name="persistence/jndidemo",
                        type=PersistenceContextType.EXTENDED)
    private EntityManager em;

    @Resource
    protected void setSessionContext(SessionContext ctx) {
        super.ctx = ctx;
    }
    
    //we will assign this value using a JNDI lookup from the injection at the
    //beginning of the class; this works in JBoss 4.0.5, but not in 4.0.4
    protected CookLocal cook; 

    @Resource(name="ejb/cook", mappedName="jndiSchedulerEAR-1.0.2007.1/CookEJB/local")
    protected CookLocal cook2; 
    
    @Resource(name="vals/message")
    String message;

    @PostConstruct
    public void init() {        
        log.info("******************* BakeScheduler Created ******************");
        log.debug("ctx=" + ctx);
        log.debug("ejb/cook=" + ctx.lookup("ejb/cook"));
        log.debug("em=" + em);
        log.debug("persistence/jndidemo=" + ctx.lookup("persistence/jndidemo"));
        log.debug("message=" + message);
        log.debug("cook=" + cook);
        log.debug("cook2=" + cook2);
        cook = (CookLocal)ctx.lookup("ejb/cook");
        try { new JNDIHelper().dump(new InitialContext(), "java:comp.ejb3");
        } catch (NamingException e) { log.fatal("" + e); }
    }
}