package Html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Element extends AbstractElement {

    public String content = "";
    private boolean isEmpty = false;

    private final List<AbstractElement> children = new ArrayList<>();

    public Element(String name) {
        super(name);
    }

    public Element(String name, String content) {
        super(name);
        this.content = content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    public void addChild(AbstractElement addedChild) {
        addedChild.parent = this;
        addedChild.updateIndentLevel(indentLevel);
        children.add(addedChild);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public int getNumChildren() {
        return children.size();
    }

    public List<AbstractElement> getChildren() {
        return Collections.unmodifiableList(children);
    }

    // recursive
    @Override
    protected void updateIndentLevel(int parentIndentLevel) {
        this.indentLevel = parentIndentLevel + 1;
        for (AbstractElement child : children) {
            child.updateIndentLevel(this.indentLevel);
        }
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder("");
        // indent
        for (int i = 0; i < indentLevel; i++) {
            sb.append("\t");
        }

        // starting tag
        sb.append("<");
        sb.append(name);
        for (Attribute attribute : attributes) {
            sb.append(" ").append(attribute);
        }
        sb.append(">");
        if (!isEmpty) {
            // content
            sb.append(content);
            // children
            for (AbstractElement child : children) {
                sb.append("\n");
                sb.append(child.print());
            }

            if (hasChildren()) {
                sb.append("\n");
                // indent
                for (int i = 0; i < indentLevel; i++) {
                    sb.append("\t");
                }
            }
            // ending tag   todo: some elements don't need ending tag
            sb.append("</").append(name).append(">");
        }
        return sb.toString();
    }

    public String toString() {
        return name;
    }
}
