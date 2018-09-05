package Invoice;

import Html.Attribute;
import Html.Element;

import java.io.Serializable;

public class Group implements ItemListEntry, Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    public Group() {
        name = "";
    }

    public Group(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Element toHtmlRow() {
        Element row = new Element("tr");
        Element cell = new Element("td", name);
        cell.addAttribute(new Attribute("colspan", "4"));
        cell.addAttribute(new Attribute("style", "border-left:1px solid black; border-right:1px solid black; font-style:italic"));
        row.addChild(cell);
        return row;
    }
}
