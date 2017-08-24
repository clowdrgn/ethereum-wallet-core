package util.nonceworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Created by fanjl on 2017/5/27.
 */
public class Test implements Runnable {
    Logger logger = LoggerFactory.getLogger(Test.class);
    @Override
    public void run() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(NonceModel.getInstance().getCurrentNonce()!=null){
            logger.info("add current nonce 0x1:10");
            NonceModel.getInstance().getCurrentNonce().put("0x1",BigInteger.TEN);
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        if(NonceModel.getInstance().getCurrentNonce()!=null){
//            logger.info("add current nonce 0x1:0");
//            NonceModel.getInstance().getCurrentNonce().put("0x2",new BigInteger("2"));
//        }
    }
}
