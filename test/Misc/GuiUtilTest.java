package Misc;

import Lib.GuiUtils;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created on 2016-12-20.
 */
public class GuiUtilTest {

    @Test
    public void testPhoneNumberFormat() throws Exception {
        Assert.assertEquals("604-657-7930", GuiUtils.formatPhoneNumber("6046577930"));
        Assert.assertEquals("604-657-7930", GuiUtils.formatPhoneNumber("604.657.7930"));
        Assert.assertEquals("604-657-7930", GuiUtils.formatPhoneNumber("604-657-7930"));
        Assert.assertEquals("604-657-7930", GuiUtils.formatPhoneNumber("604 657 7930"));
        Assert.assertEquals("604-657-7930", GuiUtils.formatPhoneNumber("657 7930"));
        Assert.assertEquals("604-657-7930", GuiUtils.formatPhoneNumber("6577930"));
        Assert.assertEquals("604-657-7930", GuiUtils.formatPhoneNumber("657.7930"));
        Assert.assertEquals("604-657-7930", GuiUtils.formatPhoneNumber("657-7930"));

        Assert.assertEquals("", GuiUtils.formatPhoneNumber(""));
        Assert.assertEquals("123", GuiUtils.formatPhoneNumber("123"));
        Assert.assertEquals("123456789123", GuiUtils.formatPhoneNumber("123456789123"));

    }

}
