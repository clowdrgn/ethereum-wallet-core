package util.nonceworker;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fanjl on 2017/5/27.
 */
public class NonceModel {
    private  volatile ConcurrentHashMap<String,BigInteger> currentNonce;
    private  volatile ConcurrentHashMap<String,BigInteger> oldNonce;
    static class InstanceHolder {
        public final static NonceModel holder = new NonceModel();
    }

    public static NonceModel getInstance() {
        return InstanceHolder.holder;
    }

    public ConcurrentHashMap<String, BigInteger> getCurrentNonce() {
        return currentNonce;
    }

    public void setCurrentNonce(ConcurrentHashMap<String, BigInteger> currentNonce) {
        this.currentNonce = currentNonce;
    }

    public ConcurrentHashMap<String, BigInteger> getOldNonce() {
        return oldNonce;
    }

    public void setOldNonce(ConcurrentHashMap<String, BigInteger> oldNonce) {
        this.oldNonce = oldNonce;
    }
}
