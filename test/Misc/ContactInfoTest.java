package Misc;

import AddressBook.ContactInfo;
import junit.framework.Assert;
import org.junit.Test;

public class ContactInfoTest {
    @Test
    public void testParse() {
        ContactInfo contactInfo = ContactInfo.parse("Nathan\n604-657-7930\nnathanzheng87@gmail.com");
        Assert.assertEquals("Nathan", contactInfo.getName());
        Assert.assertEquals("604-657-7930", contactInfo.getPhone());
        Assert.assertEquals("nathanzheng87@gmail.com", contactInfo.getEmail());

        ContactInfo contactInfo2 = ContactInfo.parse("Nathan Zheng\n\n6046577930\n\nnathanzheng87@gmail.com");
        Assert.assertEquals("Nathan Zheng", contactInfo2.getName());
        Assert.assertEquals("604-657-7930", contactInfo2.getPhone());
        Assert.assertEquals("nathanzheng87@gmail.com", contactInfo2.getEmail());

        ContactInfo contactInfo3 = ContactInfo.parse("Janet Sun\n604-657-9897\njanet.sun@shaw.ca");
        Assert.assertEquals("Janet Sun", contactInfo3.getName());
        Assert.assertEquals("604-657-9897", contactInfo3.getPhone());
        Assert.assertEquals("janet.sun@shaw.ca", contactInfo3.getEmail());
    }
}
