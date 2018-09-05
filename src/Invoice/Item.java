package Invoice;

import Html.Attribute;
import Html.Element;
import Utils.MathUtil;

import java.io.Serializable;

public class Item implements ItemListEntry, Serializable {

    private static final long serialVersionUID = 1L;

    private String name = "";
    private int quantity = 1;
    private double unitPrice = 0;
    private double price = 0;

    public Item() {
        this.name = "";
        this.quantity = 1;
        this.unitPrice = 0;
        this.price = 0;
    }

    public Item(String name, int quantity, double unitPrice) {
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.price = quantity * unitPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.price = quantity * unitPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.price = quantity * unitPrice;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public Element toHtmlRow() {
        Element row = new Element("tr");

        Element cell = new Element("td", name);
        cell.addAttribute(new Attribute("id", "tableColLeft"));
        row.addChild(cell);

        cell = new Element("td", Integer.toString(quantity));
        cell.addAttribute(new Attribute("id", "tableColMiddle"));
        row.addChild(cell);

        cell = new Element("td", MathUtil.formatCurrency(unitPrice));
        cell.addAttribute(new Attribute("id", "tableColMiddle"));
        row.addChild(cell);

        cell = new Element("td", MathUtil.formatCurrency(price));
        cell.addAttribute(new Attribute("id", "tableColRight"));
        row.addChild(cell);

        return row;
    }

    @Override
    public String toString() {
        return name;
    }

    public String summary() {
        return name + " x " + quantity + " at " + MathUtil.formatCurrency(unitPrice);
    }

    public String shortSummary() {
        return name + " x " + quantity;
    }
}
