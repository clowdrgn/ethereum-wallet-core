package util.work;


import util.web3j.wallet.ECKeyPair;

import java.util.List;

/**
 * Created by fanjl on 2017/6/1.
 */
public class WalletModel {
    private ECKeyPair ecKeyPair;
    private List<String> mnemonicCode;

    public WalletModel(ECKeyPair ecKeyPair, List<String> mnemonicCode) {
        this.ecKeyPair = ecKeyPair;
        this.mnemonicCode = mnemonicCode;
    }

    public ECKeyPair getEcKeyPair() {
        return this.ecKeyPair;
    }

    public void setEcKeyPair(ECKeyPair ecKeyPair) {
        this.ecKeyPair = ecKeyPair;
    }

    public List<String> getMnemonicCode() {
        return this.mnemonicCode;
    }

    public void setMnemonicCode(List<String> mnemonicCode) {
        this.mnemonicCode = mnemonicCode;
    }
}
