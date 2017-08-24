package util.iban;

import java.math.BigInteger;

import static jdk.nashorn.internal.runtime.GlobalFunctions.parseInt;

/**
 * Created by fanjl on 2017/6/28.
 */
public class BaseFunc {
    public static String padLeft(String src, int bytes) {
        while (src.length() < bytes * 2) {
            src = '0' + src;
        }
        return src;
    }

    public static int iso13616Prepare(String iban) {
        int A = Character.codePointAt("A", 0);
        int Z = Character.codePointAt("Z", 0);
        iban = iban.toUpperCase();
        iban = iban.substring(4) + iban.substring(0, 4);
        String[] ibans = iban.split("");
        for (char n : iban.toCharArray()) {
            int code = n;
            if (code >= A && code <= Z) {
                return code - A + 10;
            } else {
                return n;
            }
        }
        return 0;
    }

    public static int mod9710(String iban) {
        String remainder = iban;
        String block = "";
        while(remainder.length()>2){
            block = remainder.substring(0,9);
            remainder = Integer.parseInt(block,10)%97 + remainder.substring(block.length());
        }
        return Integer.parseInt(remainder,10) % 97;
    }
}
