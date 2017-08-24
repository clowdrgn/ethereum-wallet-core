package util.iban;

import util.web3j.wallet.Numeric;

import java.math.BigInteger;

/**
 * Created by fanjl on 2017/6/28.
 */
public class IbanExtend extends BaseFunc {

    public static String fromAddress(String address) {
        address = Numeric.cleanHexPrefix(address);
        BigInteger asBn = new BigInteger(address, 16);
        String base36 = asBn.toString(36);
        String padded = padLeft(base36, 15);
        return fromBban(padded.toUpperCase());
    }

    private static String fromBban(String bban) {
        String countryCode = "XE";
        int remainder = Modulo97.checksum((countryCode + "00" + bban) + "");
        String checkDigit = 98 - remainder + "";
        return countryCode + checkDigit + bban;
    }

    private static boolean isValid(String iban) {

        return Modulo97.verifyCheckDigits(iban);
    }

    public static String toAddress(String iban) {
        if (isValid(iban)) {
            String base36 = iban.substring(4);
            BigInteger asBn = new BigInteger(base36, 36);
            return padLeft(asBn.toString(16), 20);
        } else {
            throw new RuntimeException("invalid iban address:" + iban);
        }
    }

    public static void main(String[] args) {
        System.out.println(fromAddress("ac17961144fcf098f8d7ee37de9f16a57c5189ea"));
        System.out.println(toAddress("XE21K3OK0LQEZQNBED6KAYUXXPX1EEXC7HM"));
    }

}
