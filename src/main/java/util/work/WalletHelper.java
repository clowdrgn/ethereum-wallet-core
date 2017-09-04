package util.work;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.bip44.HdKeyNode;
import util.bip44.NetworkParameters;
import util.hd.Derivation;
import util.hd.ECKey;
import util.hd.ExtendedKey;
import util.mnemonic.MnemonicCode;
import util.mnemonic.MnemonicException;
import util.web3j.abi.FunctionEncoder;
import util.web3j.abi.TypeReference;
import util.web3j.abi.datatypes.Address;
import util.web3j.abi.datatypes.Function;
import util.web3j.abi.datatypes.Type;
import util.web3j.abi.datatypes.generated.Uint256;
import util.web3j.wallet.CipherException;
import util.web3j.wallet.Credentials;
import util.web3j.wallet.ECKeyPair;
import util.web3j.wallet.Hash;
import util.web3j.wallet.Keys;
import util.web3j.wallet.MnemonicModel;
import util.common.Numeric;
import util.web3j.wallet.Sign;
import util.web3j.wallet.WalletUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * Created by fanjl on 2017/5/18.
 */
public class WalletHelper {

    static Logger logger = LoggerFactory.getLogger(WalletHelper.class);
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);
    private static final BigInteger CONTRACT_GAS_LIMIT = BigInteger.valueOf(4_300_000);
    public static final BigInteger TRANSFER_GAS_LIMIT = BigInteger.valueOf(21000);
    private static BigInteger ethUnit = new BigInteger("1000000000000000000");
    private static BigInteger ugtUnit = new BigInteger("1000000000000000000");
    private static MnemonicCode mc;

    static {
        try {
            mc = new MnemonicCode() {
                @Override
                protected InputStream openWordList() throws IOException {
                    InputStream stream = MnemonicCode.class.getResourceAsStream("/english.txt");
                    if (stream == null) {
                        throw new FileNotFoundException("/english.txt");
                    }
                    return stream;
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        mc.setInstance(mc);

    }

    private static Boolean validateChecksumAddress(String address) {
        address = Numeric.cleanHexPrefix(address);
        String hash = Numeric.toHexStringNoPrefix(Hash.sha3(address.toLowerCase().getBytes()));
        for (int i = 0; i < 40; i++) {
            if (Character.isLetter(address.charAt(i))) {
                // each uppercase letter should correlate with a first bit of 1 in the hash
                // char with the same index, and each lowercase letter with a 0 bit
                int charInt = Integer.parseInt(Character.toString(hash.charAt(i)), 16);
                if (((Character.isUpperCase(address.charAt(i)) && charInt <= 7))
                        || ((Character.isLowerCase(address.charAt(i)) && charInt > 7))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String toCheckSumAddress(String address) {
        address = Numeric.cleanHexPrefix(address);
        String hash = Numeric.toHexStringNoPrefix(Hash.sha3(address.toLowerCase().getBytes()));
        String ret = "0x";
        for (int i = 0; i < 40; i++) {
            if (Character.isLetter(address.charAt(i))) {
                int charInt = Integer.parseInt(Character.toString(hash.charAt(i)), 16);
                if(charInt>7){
                    ret += Character.toString(address.charAt(i)).toUpperCase();
                }else{
                    ret += Character.toString(address.charAt(i));
                }
            }else{
                ret += Character.toString(address.charAt(i));
            }
        }
        return ret;
    }

    /**
     * 创建钱包，返回包含公私钥和助记词的walletmodel
     * isBip44=true,使用bip44协议生成
     * isBip44=false,使用bip32生成
     * @return WalletModel
     */
    public static WalletModel toMnemonic(String passphrase,boolean isBip44) throws Exception {
        try {
            Random random = new Random();
            byte[] values = new byte[16];
            random.nextBytes(values);
            List<String> mnemonicCode = mc.toMnemonic(values);
            byte[] seed = MnemonicCode.toSeed(mnemonicCode, passphrase);
            ECKeyPair ecKeyPair = null;
            if(isBip44){
                ecKeyPair = createBip44NodeFromSeed(seed);
            }else {
                ecKeyPair = getECKeyPairFromSeed(seed);
            }
            return new WalletModel(ecKeyPair, mnemonicCode);
        } catch (MnemonicException.MnemonicLengthException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 传入助记词，返回公私钥
     * isBip44=true,使用bip44协议生成
     * isBip44=false,使用bip32生成
     * @param mnemonicCode
     * @return
     */
    public static ECKeyPair fromMnemonic(List<String> mnemonicCode,String passphase,boolean isBip44) throws Exception {
        byte[] seed = mc.toSeed(mnemonicCode,passphase);
        ECKeyPair ecKeyPair = null;
        if(isBip44){
            ecKeyPair = createBip44NodeFromSeed(seed);
        }else {
            ecKeyPair = getECKeyPairFromSeed(seed);
        }
        return ecKeyPair;
    }

    /**
     * 错误的用法，直接将seed当作keyhash使用，少了一次HmacSHA512。逐渐废弃不再使用
     * @param seed
     * @return
     * @throws Exception
     */
    @Deprecated
    private static ECKeyPair getECKeyPairFromSeed(byte[] seed) throws Exception {
        ExtendedKey extendedKey = new ExtendedKey(seed);
        BigInteger pri = extendedKey.getECKey().getPriv();
        ECKeyPair ecKeyPair = ECKeyPair.create(pri);
        return ecKeyPair;
    }

    /**
     * bip44 路径为"m/44'/60'/0'／0／0"，m/44'/60'为ethereum规定的路径，均为hardened child node。
     * 其后的/0'/0/0可传入。注意第一个0'调用createHardenedChildNode,其他两个调用createChildNode
     * @param seed
     * @return
     */
    private static ECKeyPair createBip44NodeFromSeed(byte[] seed){
        HdKeyNode node = HdKeyNode.fromSeed(seed).createHardenedChildNode(44).createHardenedChildNode(60).createHardenedChildNode(0).createChildNode(0).createChildNode(0);
        byte[] privateKeyByte = node.getPrivateKey().getPrivateKeyBytes();
        ECKeyPair ecKeyPair = ECKeyPair.create(privateKeyByte);
        return ecKeyPair;
    }

    public static String generatePriKeyFile(String pwd, ECKeyPair ecKeyPair, String filePath) {
        String fileName = null;
        try {
            fileName = WalletUtils.generateWalletFile(pwd, ecKeyPair, new File(filePath), false);
        } catch (CipherException e) {
            logger.error("generate pri key file error,password is wrong ", e);
        } catch (IOException e) {
            logger.error("generate pri key file error,io exception ", e);
        }
        return fileName;
    }
    public static MnemonicModel generateMnemonicCipherText(String pwd, String mnemonicCode) {
        MnemonicModel mnemonicModel = null;
        try {
            mnemonicModel = WalletUtils.generateMnemonicCipher(pwd, mnemonicCode);
        } catch (CipherException e) {
            logger.error("generate mnemonic code cipher error ", e);
        }
        return mnemonicModel;
    }


    public static Credentials loadCredentials(String pwd,  String fileName) {
        Credentials credentials = null;
        try {
            credentials = WalletUtils.loadCredentials(pwd, fileName);
        } catch (IOException e) {
            logger.error("load pri key file error,io exception ", e);
        } catch (CipherException e) {
            logger.error("load pri key file error,password is wrong ", e);
        }
        return credentials;
    }

    public static Sign.SignatureData signMsg(String msg, ECKeyPair ecKeyPair) {
        Sign.SignatureData signatureData = Sign.signMessage(msg.getBytes(), ecKeyPair);
        return signatureData;
    }

    public static String signEthTransfer(String fromAddress,String toAddress, BigInteger value, Credentials credentials) {
//        Map<String,BigInteger> nonce = NonceModel.getInstance().getCurrentNonce();
//        BigInteger currentNonce = nonce.get(fromAddress);
        BigInteger currentNonce = new BigInteger("453");
        value = value.multiply(ethUnit);
        //从服务端获取nonce，每次签名nonce++；
        String signedTx = SignUtil.signRawTrascation(credentials, currentNonce, GAS_PRICE, TRANSFER_GAS_LIMIT, toAddress, value);
//        nonce.put(fromAddress,currentNonce.add(BigInteger.ONE));
        return signedTx;
    }

    public static String signUgtTransfer(String fromAddress,String toAddress, String contractAddress, BigInteger value, Credentials credentials) {
//        Map<String,BigInteger> nonce = NonceModel.getInstance().getCurrentNonce();
//        BigInteger currentNonce = nonce.get(fromAddress);
        BigInteger currentNonce = new BigInteger("453");
        value = value.multiply(ugtUnit);
        Address _to = new Address(toAddress);
        Uint256 _value = new Uint256(value);
        Function function = new Function("transfer", Arrays.<Type>asList(_to, _value), Collections.<TypeReference<?>>emptyList());
        String dataHex = FunctionEncoder.encode(function);
        String signData = SignUtil.signContractTrascation(credentials, currentNonce, GAS_PRICE, contractAddress, CONTRACT_GAS_LIMIT, BigInteger.ZERO, dataHex);
//        nonce.put(fromAddress,currentNonce.add(BigInteger.ONE));
        return signData;
    }

    public static String recoverAddress(String msg, Sign.SignatureData signatureData) {
        BigInteger publick = null;
        try {
            publick = Sign.signedMessageToKey(msg.getBytes(), signatureData);
        } catch (SignatureException e) {
            logger.error("verify signature data error", e);
        }
        String address = Keys.getAddress(publick);
        return Numeric.prependHexPrefix(address);
    }

    private static int[] stripLeadingZeroBytes(byte a[]) {
        int byteLength = a.length;
        int keep;

        // Find first nonzero byte
        for (keep = 0; keep < byteLength && a[keep] == 0; keep++)
            ;

        // Allocate new array and copy relevant part of input array
        int intLength = ((byteLength - keep) + 3) >>> 2;
        int[] result = new int[intLength];
        int b = byteLength - 1;
        for (int i = intLength-1; i >= 0; i--) {
            result[i] = a[b--] & 0xff;
            int bytesRemaining = b - keep + 1;
            int bytesToTransfer = Math.min(3, bytesRemaining);
            for (int j=8; j <= (bytesToTransfer << 3); j += 8)
                result[i] |= ((a[b--] & 0xff) << j);
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
//        System.out.println(toCheckSumAddress("0x66de1dc4541e5e0ff54c3d9501ccf0da36e18390"));
//        System.out.println(validateChecksumAddress("0x66de1Dc4541E5e0Ff54c3d9501CCf0da36E18390"));
//        String walletLocation = "/Users/fanjl/testkey";
        //mnemonic,生成钱包，获取到助记词和公私钥
//        long start = System.currentTimeMillis();
//        WalletModel walletModel = toMnemonic("12345678");
//        List<String> mnemonicCode = walletModel.getMnemonicCode();
//        System.out.println(mnemonicCode);
//        System.out.println("tomnemonic took {"+(System.currentTimeMillis() - start)+"}ms");
        ECKeyPair ecKeyPair = fromMnemonic(Arrays.asList("brand","course","select","lady", "note", "quiz", "slender", "antique", "shoot", "sauce", "coach", "bacon"),"",true);
//        ECKeyPair ecKeyPair1 = walletModel.getEcKeyPair();
//        String address = Keys.getAddress(walletModel.getEcKeyPair());
        String address = Keys.getAddress(ecKeyPair);
//        System.out.println(ecKeyPair.getPrivateKey());
//        System.out.println(Numeric.toHexStringNoPrefix(ecKeyPair.getPrivateKey()));
//        System.out.println(walletModel.getEcKeyPair().getPrivateKey());
//        System.out.println(Numeric.toHexStringNoPrefix(walletModel.getEcKeyPair().getPrivateKey()));
        System.out.println(address);
//
//        String fileName = generatePriKeyFile("pwd", ecKeyPair, walletLocation);
//        System.out.println(fileName);
//
//        //生成助记词的加密类MnemonicModel存数据库，使用Script加密
//        String mnemonic = "various diamond patrol humor raccoon puzzle foam orbit forest make trouble toy";
//        MnemonicModel mnemonicModel = generateMnemonicCipherText("pwd",mnemonic);
//        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
//        String result = objectMapper.writeValueAsString(mnemonicModel);
//        String code = WalletUtils.loadMnemonicCipher("pwd",mnemonicModel);
//        System.out.println(code);
//        System.out.println(result);
//        Credentials credentials = loadCredentials("12345678",  "/Users/fanjl/testkey");
//        System.out.println(credentials.getEcKeyPair().getPrivateKey());
//        System.out.println(Keys.getAddress(credentials.getEcKeyPair()));
//        Sign.SignatureData signatureData = signMsg("test", ecKeyPair);
//        String address = recoverAddress("test", signatureData);
//
//        System.out.println(address);
//        System.out.println(credentials.getAddress());
//        String ethResult = signEthTransfer("0xc6e76208d475844d2ac31744bd2a6420031fd066","0x123", new BigInteger("100"), credentials);
//        String ugtResult = signUgtTransfer("0xc6e76208d475844d2ac31744bd2a6420031fd066", "0xe4a250c98b3f004cfea01555b943ef5bda06b5d0", "0x88990bb5bc0aee3bd926fc23faa3fff3c29ff970",new BigInteger("100"), credentials);
//
//        System.out.println(ethResult);
//        System.out.println(ethResult.length());
    }

}
