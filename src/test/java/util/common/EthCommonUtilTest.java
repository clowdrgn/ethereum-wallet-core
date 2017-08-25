package util.common;

import org.junit.Test;
import util.base.BaseTestConstant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by fanjl on 2017/8/25.
 */
public class EthCommonUtilTest extends BaseTestConstant{
    @Test
    public void testValidateEthAddress() {
        assertTrue(EthCommonUtil.validateEthAddress(FORM_ADDRESS));
        assertFalse(EthCommonUtil.validateEthAddress(UNFORM_ADDRESS));
    }
}
