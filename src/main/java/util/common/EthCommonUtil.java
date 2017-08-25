package util.common;

import java.util.regex.Pattern;

/**
 * Created by fanjl on 2017/8/25.
 */
public class EthCommonUtil {

    private static final String ADDRESS_PATTERN="^0x[a-fA-F0-9]{40}$";

    public static boolean validateEthAddress(String address){
        address = Numeric.prependHexPrefix(address);
        return  Pattern.matches(ADDRESS_PATTERN, address);
    }
}
