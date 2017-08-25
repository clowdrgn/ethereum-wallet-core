package util.iban;

import com.sun.tools.corba.se.idl.constExpr.Modulo;
import util.common.EthCommonUtil;
import util.common.Numeric;

import java.math.BigInteger;

/**
 * Created by fanjl on 2017/6/28.
 */
public class IbanUtil {

    private static final String COUNTRY_CODE="XE";

    /**
     * convert the given address to iban form.Firstly,clean 0x prefix address,then convert that
     * to 16 radix BigInteger. Next transfer bigInteger to 36 radix String and padded left with '0'
     * to 30 byte we called bban.finally get the check digits and put conuntry code, check digitsthem
     * and bban together. <p>See the <a href="https://en.wikipedia.org/wiki/International_Bank_Account_Number">
     * specification</a> for further information.
     * @param address the input to convert, it must be a ethereum address with 40 character hex string.
     * @return iban form address start with XE country code and followed by checksum.
     * @throws IllegalArgumentException if the input is in some way invalid.
     */
    public static String fromAddress(String address) {
        if(!EthCommonUtil.validateEthAddress(address)){
            throw new IllegalArgumentException("not a valid 40 hex string");
        }
        address = Numeric.cleanHexPrefix(address);
        BigInteger asBn = new BigInteger(address, 16);
        String base36 = asBn.toString(36);
        String bban = padLeft(base36, 15).toUpperCase();
        String bbanCheck = new StringBuilder(COUNTRY_CODE).append("00").append(bban).toString();
        String checkDigit = Modulo97.calculateCheckDigits(bbanCheck) + "";
        String iban = new StringBuilder(COUNTRY_CODE).append(checkDigit).append(bban).toString();
        return iban;
    }

    /**
     * validate the iban checksu,if return false, the client should remind users to notice
     * @param iban
     * @return
     */
    private static boolean isValid(String iban) {
        return Modulo97.verifyCheckDigits(iban);
    }

    /**
     * convert iban form address to 40 hex string address.Validate the checksum first.
     * @param iban
     * @throws IllegalArgumentException if the input checksum invalid.
     * @return 40 hex String with 0x prefix
     */
    public static String toAddress(String iban) {
        if (isValid(iban)) {
            String base36 = iban.substring(4);
            BigInteger asBn = new BigInteger(base36, 36);
            String padd = padLeft(asBn.toString(16),20);
            String address = Numeric.prependHexPrefix(padd);
            return address;
        } else {
            throw new IllegalArgumentException("invalid iban address:" + iban);
        }
    }

    /**
     * pad src left with '0' to the aim bytes.Used in iban convert .
     * @param src
     * @param bytes
     * @return
     */
    public static String padLeft(String src, int bytes) {
        while (src.length() < bytes * 2) {
            src = '0' + src;
        }
        return src;
    }

}
