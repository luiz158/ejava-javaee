package myorg.queryex.criteria;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TemporalType;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import myorg.queryex.Actor;
import myorg.queryex.Director;
import myorg.queryex.Movie;
import myorg.queryex.MovieRating;
import myorg.queryex.MovieRole;
import myorg.queryex.Person;
import myorg.queryex.QueryBase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CriteriaTest extends QueryBase {
	private static final Log log = LogFactory.getLog(CriteriaTest.class);
	private EntityManager em2; //second persistence context for parallel queries
	
	@Before
	public void init() {
		if (emf!=null) {
			em2=emf.createEntityManager();
		}
	}
	
	@After
	public void destroy() {
		if (em2!=null && em2.isOpen()) {
			if (em2.getTransaction().isActive()) {
				if (em2.getTransaction().getRollbackOnly()) {
					em2.getTransaction().rollback();
				} else {
					em2.getTransaction().commit();
				}
			}
			em2.close();
		}
	}
	

	/**
	 * This test demonstrates a basic criteria query as a starting point.
	 * Notice how "p" is designed as the query root in both styles of query
	 * and all expressions from that point are derived from the root.
	 */
	@Test
	public void testBasicQuery() {
		log.info("*** testBasicQuery ***");
		//build JPAQL query
		StringBuilder qlString = new StringBuilder()
			.append("select p from Person p ")
			.append("where p.firstName=:firstName");
		TypedQuery<Person> lquery = em.createQuery(qlString.toString(), Person.class)
			.setParameter("firstName", "Ron");
		
		//build criteria API query
		CriteriaBuilder cb = em2.getCriteriaBuilder();
		CriteriaQuery<Person> cqdef = cb.createQuery(Person.class);
		Root<Person> p = cqdef.from(Person.class); 
		TypedQuery<Person> cquery = em2.createQuery(cqdef
				.select(p)
				.where(cb.equal(p.get("firstName"), "Ron")));
		
		log.debug("execute JPAQL query");
		List<Person> lresults = lquery.getResultList();
		log.debug("accessing jpaql results");
		log.debug("jpaql results  =" + lresults);
		
		log.debug("execute Criteria API query");
		List<Person> cresults = cquery.getResultList();		
		log.debug("accessing criteria results");
		log.debug("critera results=" + cresults);
		
		log.debug("comparing query results");
		assertEquals("unexpected results",  lresults, cresults);
	}
	
	/**
	 * This test demonstrates the use of "from" in the Criteria to define 
	 * multiple query roots. In this example we are querying from both 
	 * Director and Person entities and forming our query based off these 
	 * query roots.
	 */
	@Test
	public void testFrom() {
		log.info("*** testFrom ***");
		
		//build JPAQL query
		StringBuilder qlString = new StringBuilder()
			.append("select d from Director d, " +  //from
					"Person p ")                    //from
			.append("where " +
					"p=d.person and " +
					"p.firstName=:firstName");
		TypedQuery<Director> lquery = em.createQuery(qlString.toString(), Director.class)
			.setParameter("firstName", "Ron");
		
		//build criteria API query
		CriteriaBuilder cb = em2.getCriteriaBuilder();
		CriteriaQuery<Director> cqdef = cb.createQuery(Director.class);
		Root<Director> d = cqdef.from(Director.class); //from
		Root<Person> p = cqdef.from(Person.class);     //from
		TypedQuery<Director> cquery = em2.createQuery(cqdef
				.select(d)
				.where(cb.and(
						cb.equal(d.get("person"), p)), 
						cb.equal(p.get("firstName"), "Ron"))
						);
		
		log.debug("execute JPAQL query");
		List<Director> lresults = lquery.getResultList();
		log.debug("accessing jpaql results");
		log.debug("jpaql results  =" + lresults);
		em.clear();
		
		log.debug("execute Criteria API query");
		List<Director> cresults = cquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("critera results=" + cresults);
		
		log.debug("comparing query results");
		assertEquals("unexpected results",  lresults, cresults);
	}

	/**
	 * This test demonstrates the use of a path expression. JPAQL uses a 
	 * name(dot)name path notation. The criteria API uses a changed set of 
	 * get(name).get(name).
	 */
	@Test
	public void testPath() {
		log.info("*** testPath ***");
		
		//build JPAQL query
		StringBuilder qlString = new StringBuilder()
			.append("select d from Director d ")
			.append("where " +
					"d.person.firstName" +  //Path expression
					"=:firstName");
		TypedQuery<Director> lquery = em.createQuery(qlString.toString(), Director.class)
			.setParameter("firstName", "Ron");
		
		//build criteria API query
		CriteriaBuilder cb = em2.getCriteriaBuilder();
		CriteriaQuery<Director> cqdef = cb.createQuery(Director.class);
		Root<Director> d = cqdef.from(Director.class);
		TypedQuery<Director> cquery = em2.createQuery(cqdef
				.select(d)//non-portable to eliminate redundant select() matching from()
				.where(cb.equal( 
						d.get("person").get("firstName") //Path expression
						,"Ron"))
					);
		
		log.debug("execute JPAQL query");
		List<Director> lresults = lquery.getResultList();
		log.debug("jpaql results  =" + lresults);
		log.debug("accessing criteria results");
		
		log.debug("execute Criteria API query");
		List<Director> cresults = cquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("critera results=" + cresults);

		log.debug("comparing query results");
		assertEquals("unexpected results",  lresults, cresults);
	}
	
	/**
	 * This test demonstrates the use of select() to be passed a path 
	 * expression instead of the query root. This also demonstrates the
	 * use of setting distinct against the CriteriaQuery. The path expressions
	 * require a template parameter that expresses the return type of the 
	 * query -- which is not the query root type in this case.
	 */
	@Test
	public void testSingleSelect() {
		log.info("*** testSingleSelect ***");
		
		//build JPAQL query
		StringBuilder qlString = new StringBuilder()
				//passing an path expression to select rather than root
			.append("select distinct p.firstName " +     //select
					"from Person p ")
			.append("where p.firstName like :pattern");
		TypedQuery<String> lquery = em.createQuery(qlString.toString(), String.class)
			.setParameter("pattern", "R%");
		
		//build criteria API query
		CriteriaBuilder cb = em2.getCriteriaBuilder();
		CriteriaQuery<String> cqdef = cb.createQuery(String.class);
		Root<Person> p = cqdef.from(Person.class);
		TypedQuery<String> cquery = em2.createQuery(cqdef
				//passing an path expression to select() rather than root
			.select(p.<String>get("firstName"))   //select
			.distinct(true)
				//path expressions must agree with type used in createQuery
			.where(cb.like(p.<String>get("firstName"),"R%"))
			);
		
		log.debug("execute JPAQL query");
		List<String> lresults = lquery.getResultList();
		log.debug("jpaql results  =" + lresults);
		log.debug("accessing criteria results");
		
		log.debug("execute Criteria API query");
		List<String> cresults = cquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("critera results=" + cresults);

		log.debug("comparing query results");
		assertEquals("unexpected results",  lresults, cresults);
	}

	/**
	 * This test demonstrates the ability to provide multiple elements in the 
	 * select clause. In this example we are also ordering the results so they
	 * can be accurately compared. In this specific example -- we use an Object[]
	 * for the query return type.
	 */
	@Test
	public void testMultiSelect() {
		log.info("*** testMultiSelect ***");
		
		//build JPAQL query
		StringBuilder qlString = new StringBuilder()
			.append("select distinct p.firstName, p.lastName " +
					"from Person p ")
			.append("where p.firstName like :pattern " +
					"order by p.firstName ASC");
		TypedQuery<Object[]> lquery = em.createQuery(qlString.toString(), Object[].class)
			.setParameter("pattern", "R%");
		
		//build criteria API query
		CriteriaBuilder cb = em2.getCriteriaBuilder();
		CriteriaQuery<Object[]> cqdef = cb.createQuery(Object[].class);
		Root<Person> p = cqdef.from(Person.class);
		TypedQuery<Object[]> cquery = em2.createQuery(cqdef
				.select(cb.array(p.get("firstName"), p.get("lastName")))  
				.distinct(true)
				.where(cb.like(p.<String>get("firstName"),"R%"))
				.orderBy(cb.asc(p.get("firstName")))
			);
		
		log.debug("execute JPAQL query");
		List<Object[]> lresults = lquery.getResultList();
		log.debug("accessing criteria results");
		for (Object[] row: lresults) {
			log.debug("jpaql results  =" + row[0] + " " + row[1]);
		}
		
		log.debug("execute Criteria API query");
		List<Object[]> cresults = cquery.getResultList();
		log.debug("accessing criteria results");
		for (Object[] row: lresults) {
			log.debug("criteria results  =" + row[0] + " " + row[1]);
		}

		log.debug("comparing query results");
		Iterator<Object[]> litr = lresults.iterator();
		Iterator<Object[]> citr = cresults.iterator();
		while (litr.hasNext() && citr.hasNext()) {
			Object[] lrow = litr.next(); Object[] crow = citr.next();
			for (int i=0; i<2; i++) {
				assertEquals("unexpected tuple value:" + i, lrow[i], crow[i]);
			}
		}
	}

	static private class FirstLast {
		public String first;
		public String last;
		@SuppressWarnings("unused")
		public FirstLast(String first, String last) {
			this.first = first;
			this.last = last;
		}
		public String toString() { return first + " " + last; }
	}

	/**
	 * This test is the same as the prior example except that it used a constructor
	 * of a transient object to return type-safe results over the user of Object[]
	 */
	@Test
	public void testMultiSelectConstructorExpression() {
		log.info("*** testMultiSelectConstructorExpression ***");
		
		//build JPAQL query
		StringBuilder qlString = new StringBuilder()
			.append(String.format("select distinct new %s(p.firstName, p.lastName) ", 
					FirstLast.class.getName()) +
					"from Person p ")
			.append("where p.firstName like :pattern " +
					"order by p.firstName ASC");
		TypedQuery<FirstLast> lquery = em.createQuery(qlString.toString(), FirstLast.class)
			.setParameter("pattern", "R%");
		
		//build criteria API query
		CriteriaBuilder cb = em2.getCriteriaBuilder();
		CriteriaQuery<FirstLast> cqdef = cb.createQuery(FirstLast.class);
		Root<Person> p = cqdef.from(Person.class);
		TypedQuery<FirstLast> cquery = em2.createQuery(cqdef
				.select(cb.construct(FirstLast.class, p.get("firstName"), p.get("lastName")))  
				//.multiselect(p.get("firstName"), p.get("lastName")) //shorthand	  
				.distinct(true)
				.where(cb.like(p.<String>get("firstName"),"R%"))
				.orderBy(cb.asc(p.get("firstName")))
			);
		
		log.debug("execute JPAQL query");
		List<FirstLast> lresults = lquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("jpaql results   =" + lresults);
		
		log.debug("execute Criteria API query");
		List<FirstLast> cresults = cquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("criteria results=" + cresults);

		log.debug("comparing query results");
		Iterator<FirstLast> litr = lresults.iterator();
		Iterator<FirstLast> citr = cresults.iterator();
		while (litr.hasNext() && citr.hasNext()) {
			FirstLast lrow = litr.next(); FirstLast crow = citr.next();
			assertEquals("unexpected tuple value:first", lrow.first, crow.first);
			assertEquals("unexpected tuple value:last", lrow.last, crow.last);
		}
	}

	/**
	 * This example is the same as before except it is using the JPA Tuple interface
	 * for the return type.
	 */
	@Test
	public void testMultiSelectTuple() {
		log.info("*** testMultiSelectTuple ***");
		
		//build JPAQL query
		StringBuilder qlString = new StringBuilder()
			.append("select distinct p.firstName as fname, p.lastName as lname " +
					"from Person p ")
			.append("where p.firstName like :pattern " +
					"order by p.firstName ASC");
		TypedQuery<Tuple> lquery = em.createQuery(qlString.toString(), Tuple.class)
			.setParameter("pattern", "R%");
		
		//build criteria API query
		CriteriaBuilder cb = em2.getCriteriaBuilder();
		CriteriaQuery<Tuple> cqdef = cb.createTupleQuery();
		Root<Person> p = cqdef.from(Person.class);
		TypedQuery<Tuple> cquery = em2.createQuery(cqdef
				.select(cb.tuple(
						p.get("firstName").alias("fname"), 
						p.get("lastName").alias("lname"))) 
				//.multiselect(  //shorthand
				//		p.get("firstName").alias("fname"), 
				//		p.get("lastName").alias("lname")) 
				.distinct(true)
				.where(cb.like(p.<String>get("firstName"),"R%"))
				.orderBy(cb.asc(p.get("firstName")))
			);
		
		log.debug("execute JPAQL query");
		List<Tuple> lresults = lquery.getResultList();
		log.debug("accessing criteria results");
		for (Tuple t: lresults) {
			log.debug("jpaql results  =" + 
					t.get("fname",String.class) + " " + 
					t.get("lname", String.class));
		}
		
		log.debug("execute Criteria API query");
		List<Tuple> cresults = cquery.getResultList();
		log.debug("accessing criteria results");
		for (Tuple t: cresults) {
			log.debug("criteria results  =" + 
					t.get("fname",String.class) + " " + 
					t.get("lname", String.class));
		}

		log.debug("comparing query results");
		Iterator<Tuple> litr = lresults.iterator();
		Iterator<Tuple> citr = cresults.iterator();
		while (litr.hasNext() && citr.hasNext()) {
			Tuple lt = litr.next(); Tuple ct = citr.next();
			for (String alias: new String[]{"fname","lname"}) {
				assertEquals("unexpected tuple value:" + alias, 
					lt.get(alias,String.class), ct.get(alias, String.class));
			}
		}
	}

	/**
	 * This test method demonstrates extending the query of the root to 
	 * related objects through a join. 
	 */
	@Test
	public void testJoin() {
		log.info("*** testJoin ***");
		
		//build JPAQL query
		StringBuilder qlString = new StringBuilder()
			.append("select distinct m from Movie m " +
					"LEFT JOIN m.director d " +
					"LEFT JOIN d.person p ")
			.append("where p.firstName=:firstName ")
			.append("order by m.releaseDate ASC ");
		TypedQuery<Movie> lquery = em.createQuery(qlString.toString(), Movie.class)
			.setParameter("firstName", "Ron");
		
		//build criteria API query
		CriteriaBuilder cb = em2.getCriteriaBuilder();
		CriteriaQuery<Movie> cqdef = cb.createQuery(Movie.class);
		Root<Movie> m = cqdef.from(Movie.class);
		Join<Movie, Director> d = m.join("director", JoinType.LEFT);
		Join<Director, Person> p = d.join("person", JoinType.LEFT);
		TypedQuery<Movie> cquery = em2.createQuery(cqdef
				.select(m)
				.distinct(true)
				.where(cb.equal(p.get("firstName"), "Ron"))
				.orderBy(cb.asc(m.get("releaseDate")))
			);
		
		log.debug("execute JPAQL query");
		List<Movie> lresults = lquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("jpaql results  =" + lresults); 
		
		log.debug("execute Criteria API query");
		List<Movie> cresults = cquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("criteria results  =" +  cresults);

		log.debug("comparing query results");
		Iterator<Movie> litr = lresults.iterator();
		Iterator<Movie> citr = cresults.iterator();
		while (litr.hasNext() && citr.hasNext()) {
			Movie lm = litr.next(); Movie cm = citr.next();
			assertTrue(String.format("unequal movies: (%s) (%s)", lm, cm), 
					lm.equals(cm));
			assertTrue(String.format("unequal directors: (%s) (%s)", 
					lm.getDirector() , cm.getDirector()), 
					lm.getDirector().equals(cm.getDirector()));
			Iterator<MovieRole> lmrItr = lm.getCast().iterator();
			Iterator<MovieRole> cmrItr = cm.getCast().iterator();
			while (lmrItr.hasNext() && cmrItr.hasNext()) {
				MovieRole lmr=lmrItr.next(); MovieRole cmr=cmrItr.next();
				assertTrue(String.format("unequal role: (%s) (%s)", lmr , cmr), 
						lmr.equals(cmr));
			}
		}
		em2.close();
	}

	/**
	 * This class demonstrates the use of a FETCH JOIN -- where related 
	 * entities are retrieved from the database as a side-effect of executing
	 * the query. 
	 */
	@Test
	@SuppressWarnings("unused")
	public void testFetchJoin() {
		log.info("*** testFetchJoin ***");

		//build JPAQL query
		StringBuilder qlString = new StringBuilder()
			.append("select distinct m from Movie m " +
					"LEFT JOIN FETCH m.director d " +
					"LEFT JOIN FETCH m.cast role " +
					"LEFT JOIN FETCH d.person dp " +
					"LEFT JOIN FETCH role.actor actor " +
					"LEFT JOIN FETCH actor.person ap ")
			.append("order by m.releaseDate ASC");
		TypedQuery<Movie> lquery = em.createQuery(qlString.toString(), Movie.class);
		
		//build criteria API query
		CriteriaBuilder cb = em2.getCriteriaBuilder();
		CriteriaQuery<Movie> cqdef = cb.createQuery(Movie.class);
		Root<Movie> m = cqdef.from(Movie.class);
		Fetch<Movie, Director> d = m.fetch("director", JoinType.LEFT);
		Fetch<Director, Person> dp = d.fetch("person", JoinType.LEFT);
		Fetch<Movie, MovieRole> role = m.fetch("cast", JoinType.LEFT);
		Fetch<MovieRole, Actor> actor = role.fetch("actor", JoinType.LEFT);
		Fetch<Actor, Person> ap = actor.fetch("person", JoinType.LEFT);
		TypedQuery<Movie> cquery = em2.createQuery(cqdef
				.select(m)
				.distinct(true)
				.orderBy(cb.asc(m.get("releaseDate")))
			);
		
		log.debug("execute JPAQL query");
		List<Movie> lresults = lquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("jpaql results  =" + lresults); 
		
		log.debug("execute Criteria API query");
		List<Movie> cresults = cquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("criteria results  =" +  cresults);

		log.debug("closing entity managers since all data FETCHed");
		em.close(); em=null; em2.close();
		
		log.debug("comparing query results");
		Iterator<Movie> litr = lresults.iterator();
		Iterator<Movie> citr = cresults.iterator();
		while (litr.hasNext() && citr.hasNext()) {
			Movie lm = litr.next(); Movie cm = citr.next();
			assertTrue(String.format("unequal movies: (%s) (%s)", lm, cm), 
					lm.equals(cm));
			assertTrue(String.format("unequal directors: (%s) (%s)", 
					lm.getDirector() , cm.getDirector()), 
					lm.getDirector().equals(cm.getDirector()));
			Iterator<MovieRole> lmrItr = lm.getCast().iterator();
			Iterator<MovieRole> cmrItr = cm.getCast().iterator();
			while (lmrItr.hasNext() && cmrItr.hasNext()) {
				MovieRole lmr=lmrItr.next(); MovieRole cmr=cmrItr.next();
				assertTrue(String.format("unequal role: (%s) (%s)", lmr , cmr), 
						lmr.equals(cmr));
			}
		}
	}

	/**
	 * This test demonstrates use of the were clause with a single boolean
	 * expression.
	 */
	@Test
	public void testWhereBooleanExpression() {
		log.info("*** testWhereBooleanExpression ***");
		
		//build JPAQL query
		StringBuilder qlString = new StringBuilder()
			.append("select distinct m from Movie m " +
					"where m.rating=:rating ")
			.append("order by m.releaseDate ASC");
		TypedQuery<Movie> lquery = em.createQuery(qlString.toString(), Movie.class)
			.setParameter("rating", MovieRating.R);
		
		//build criteria API query
		CriteriaBuilder cb = em2.getCriteriaBuilder();
		CriteriaQuery<Movie> cqdef = cb.createQuery(Movie.class);
		Root<Movie> m = cqdef.from(Movie.class);
		TypedQuery<Movie> cquery = em2.createQuery(cqdef
				.select(m)
				.where(cb.equal(m.get("rating"), MovieRating.R))
				.orderBy(cb.asc(m.get("releaseDate")))
			);
		
		log.debug("execute JPAQL query");
		List<Movie> lresults = lquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("jpaql results  =" + lresults); 
		
		log.debug("execute Criteria API query");
		List<Movie> cresults = cquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("criteria results  =" +  cresults);

		log.debug("comparing query results");
		Iterator<Movie> litr = lresults.iterator();
		Iterator<Movie> citr = cresults.iterator();
		while (litr.hasNext() && citr.hasNext()) {
			Movie lm = litr.next(); Movie cm = citr.next();
			assertTrue(String.format("unequal movies: (%s) (%s)", lm, cm), 
					lm.equals(cm));
		}
		em2.close();
	}

	@Test @Ignore
	public void testWherePredicates() {
		log.info("*** testWherePredicates ***");
		
		//build JPAQL query
		StringBuilder qlString = new StringBuilder()
			.append("select distinct m from Movie m " +
					"where m.rating=:rating ")
			.append("order by m.releaseDate ASC");
		TypedQuery<Movie> lquery = em.createQuery(qlString.toString(), Movie.class)
			.setParameter("rating", MovieRating.R);
		
		//build criteria API query
		CriteriaBuilder cb = em2.getCriteriaBuilder();
		CriteriaQuery<Movie> cqdef = cb.createQuery(Movie.class);
		Root<Movie> m = cqdef.from(Movie.class);
		TypedQuery<Movie> cquery = em2.createQuery(cqdef
				.select(m)
				.where(cb.and(
						cb.equal(m.get("rating"), MovieRating.R)
					   ),
					   cb.or(
						cb.equal(m.get("rating"), MovieRating.R)
					   )
			     )
				.orderBy(cb.asc(m.get("releaseDate")))
			);
		
		log.debug("execute JPAQL query");
		List<Movie> lresults = lquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("jpaql results  =" + lresults); 
		
		log.debug("execute Criteria API query");
		List<Movie> cresults = cquery.getResultList();
		log.debug("accessing criteria results");
		log.debug("criteria results  =" +  cresults);

		log.debug("comparing query results");
		Iterator<Movie> litr = lresults.iterator();
		Iterator<Movie> citr = cresults.iterator();
		while (litr.hasNext() && citr.hasNext()) {
			Movie lm = litr.next(); Movie cm = citr.next();
			assertTrue(String.format("unequal movies: (%s) (%s)", lm, cm), 
					lm.equals(cm));
		}
		em2.close();
	}
}