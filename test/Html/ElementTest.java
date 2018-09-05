package Html;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ElementTest {

    @Test
    public void testPrintSimple() {
        System.out.println("\nTesting print simple...");
        Element element = new Element("p");
        element.addAttribute(new Attribute("id", "blah"));
        element.setContent("hello");
        String printValue = element.print();
        System.out.println(printValue);
        String expected = "<p id=\"blah\">hello</p>";
        assertEquals(expected, printValue);

        Element br = new Element("br");
        br.setEmpty(true);
        System.out.println(br.print());
        assertEquals("<br>", br.print());
    }

    @Test
    public void testPrintWithChildren() {
        System.out.println("\nTesting print with children...");
        Element html = new Element("html");
        Element head = new Element("head");
        Element body = new Element("body");
        html.addChild(head);
        html.addChild(body);

        Element table = new Element("table");
        Element tr = new Element("tr");
        Element td1 = new Element("td", "1");
        Element td2 = new Element("td2", "2");
        table.addChild(tr);
        tr.addChild(td1);
        tr.addChild(td2);
        body.addChild(table);

        String printValue = html.print();
        System.out.println(printValue);
    }

}
