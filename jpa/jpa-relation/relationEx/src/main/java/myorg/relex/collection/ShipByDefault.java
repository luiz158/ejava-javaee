package myorg.relex.collection;

import javax.persistence.*;

/**
 * This class is provides an example of an entity that implements hashcode/equals 
 * using the default java.lang.Object implementation. Note this implementation is instance-specific. 
 */
@Entity
@Table(name="RELATIONEX_SHIP")
public class ShipByDefault extends Ship {
	@Override
	public int peekHashCode() {
		return super.objectHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		try {
			boolean equals = super.equals(obj);
			return logEquals(obj, equals);
		} catch (Exception ex) {
			return logEquals(obj, false);
		}
	}
}
