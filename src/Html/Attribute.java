package Html;

/**
 * HTML attributes.
 * <p>
 *     This represents the attributes used by HTML elements. Each attribute consists of a pair of String fields:
 *     <ul>
 *         <li>name - the name of the attribute</li>
 *         <li>value - the value of the attribute</li>
 *     </ul>
 *     Both 'name' and 'value' and final and is to be set by the constructor. The value cannot be changed after
 *     object instantiation, create a new instance if the value need to change.
 *     <br><br>When used in collections, two attribute are considered equal if they have <b>the same name</b>.
 * </p>
 */
public class Attribute {

    public final String name;
    public final String value;

    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Changes the value of an attribute by returning an new instance of Attribute.
     * @param attribute whose value is to be changed.
     * @param value the new value to the input attribute.
     * @return an new instance of Attribute with the same name and the new value.
     */
    public static Attribute changeValue(Attribute attribute, String value) {
        return new Attribute(attribute.name, value);
    }

    @Override
    public String toString() {
        return name + "=\"" + value + "\"";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attribute attribute = (Attribute) o;

        return !(name != null ? !name.equals(attribute.name) : attribute.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
