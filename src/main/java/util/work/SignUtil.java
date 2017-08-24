package util.work;


import util.web3j.transcation.RawTransaction;
import util.web3j.transcation.TransactionEncoder;
import util.web3j.wallet.Credentials;
import util.web3j.wallet.Numeric;

import java.math.BigInteger;


/**
 * Utility functions for working with Wallet files.
 */
public class SignUtil {

    public static String signRawTrascation(Credentials credentials, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to,
                                           BigInteger value){
        RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(
                nonce, gasPrice, gasLimit, to, value);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        return  hexValue;
    }

    public static String signContractTrascation(Credentials credentials, BigInteger nonce, BigInteger gasPrice,String to, BigInteger gasLimit,
                                           BigInteger value,String data){
        RawTransaction rawTransaction  = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit,  to,value, data);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        return  hexValue;
    }

}
