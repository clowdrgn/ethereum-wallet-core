package util.iban;

import org.junit.Test;
import util.base.BaseTestConstant;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by fanjl on 2017/8/25.
 */

public class IbanUtilTest extends BaseTestConstant{
    @Test
    public void testFromAddress() {
        assertThat(IbanUtil.fromAddress(FORM_ADDRESS),equalTo(IBAN_ADDRESS));

    }
    @Test
    public void testToAddress() {
        assertThat(IbanUtil.toAddress(IBAN_ADDRESS),equalTo(FORM_ADDRESS));
    }
    @Test
    public void testPadLeft() {
        assertThat(IbanUtil.padLeft(UNFORM_ADDRESS,20),equalTo(PADDED_UNFORM_ADDRESS));
    }
}
