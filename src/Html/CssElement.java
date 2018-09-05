package Html;

public class CssElement extends AbstractElement {

    public final boolean isCustom;

    public CssElement(String name) {
        super(name);
        isCustom = true;
    }

    public CssElement(String name, boolean isCustom) {
        super(name);
        this.isCustom = isCustom;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder("");
        // indent
        for (int i = 0; i < indentLevel; i++) {
            sb.append("\t");
        }

        // name and attributes
        if (isCustom) {
            sb.append("#");
        }
        sb.append(name);
        assert !attributes.isEmpty();
        sb.append(" {");
        for (Attribute attribute : attributes) {
            sb.append(attribute.name).append(":").append(attribute.value).append("; ");
        }
        sb.append("}");

        return sb.toString();
    }

    @Override
    protected void updateIndentLevel(int parentIndentLevel) {
        indentLevel = parentIndentLevel + 1;
    }
}
