package Html;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractElement {

    public final String name;
    public final List<Attribute> attributes = new ArrayList<Attribute>();
    @Nullable
    protected AbstractElement parent;
    protected int indentLevel = 0;

    protected AbstractElement(String name) {
        this.name = name;
    }

    public boolean addAttribute(Attribute attribute) {
        return attributes.add(attribute);
    }

    public boolean removeAttribute(Attribute attribute) {
        return attributes.remove(attribute);
    }

    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public abstract String print();

    protected abstract void updateIndentLevel(int parentIndentLevel);
}
