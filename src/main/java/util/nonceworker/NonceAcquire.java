package util.nonceworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fanjl on 2017/5/27.
 */
public class NonceAcquire implements Runnable {

    Logger logger = LoggerFactory.getLogger(NonceAcquire.class);

    @Override
    public void run() {
        NonceModel nonce = NonceModel.getInstance();
        ConcurrentHashMap<String,BigInteger> concurrentNonceMap = nonce.getCurrentNonce();
        if (concurrentNonceMap == null) {
            Map<String,BigInteger> result = getFromServer("12","321");
            ConcurrentHashMap<String,BigInteger> newNonce = new ConcurrentHashMap<>();
            ConcurrentHashMap<String,BigInteger> oldNonce = new ConcurrentHashMap<>();
            oldNonce.putAll(result);
            newNonce.putAll(result);
            nonce.setCurrentNonce(newNonce);
            nonce.setOldNonce(oldNonce);
            logger.info("first timer start,get nonce from server: " + newNonce);
        } else {
            for(Map.Entry entry : concurrentNonceMap.entrySet()){
                String address = (String)entry.getKey();
                BigInteger value = (BigInteger)entry.getValue();
                if(nonce.getOldNonce().get(address)==null){
                    nonce.getOldNonce().put(address,value);
                }
                switch (value.compareTo(nonce.getOldNonce().get(address))) {
                    case 0:
                        BigInteger newNonce = getFromServerByAddress(address);
                        nonce.getCurrentNonce().put(address,newNonce);
                        nonce.getOldNonce().put(address,newNonce);
                        logger.info("The nonce not change in fix range time,get nonce from server: " + newNonce);
                        break;
                    case 1:
                        logger.info("The current nonce is bigger than old nonce,do nothing: " + nonce.getCurrentNonce().get(address));
                        break;
                    case -1:
                        logger.error("The current nonce is less than old nonce,all transaction will be fail! old nonce: " + nonce.getOldNonce().get(address) + "; current nonce: " + nonce.getCurrentNonce().get(address));
                        break;
                }
            }

        }

    }

    private Map<String,BigInteger> getFromServer(String... adress) {
        Map<String,BigInteger> nonceMap = new ConcurrentHashMap<>();
        nonceMap.put("0x1",BigInteger.ZERO);
        nonceMap.put("0x2",BigInteger.ONE);
        nonceMap.put("0x1231323dsfsdsd",new BigInteger("2"));
        return nonceMap;
    }
    private BigInteger getFromServerByAddress(String address) {
        if(address.equals("0x1")){
            return new BigInteger("2");
        }
        if(address.equals("0x2")){
            return new BigInteger("3");
        }
        if(address.equals("0x1231323dsfsdsd")){
            return new BigInteger("4");
        }
        return null;
    }

}
