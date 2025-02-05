package dev.kumru.bitcoin;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.util.Arrays;

@Slf4j
public class BitcoinUtils {


    /**
     * Verbatim from https://github.com/electrumj/electrumj/blob/main/src/main/java/org/electrumj/Util.java
     * <p>
     * Obtains the electrum's scriptshash given an address.
     * See <a href="https://electrumx-spesmilo.readthedocs.io/en/latest/protocol-basics.html#script-hashes"Electrum documentation on script hash</a>.
     *
     * @param bitcoinjNetowrkParameters The network being used: e.g. regtest, mainnet, etc.
     * @param address                   The address
     * @return The scripthash associated with the supplied address
     * @throws Throwable
     */
    public static String scripthash(NetworkParameters bitcoinjNetowrkParameters, String address) {
        Address addressBitcoinj = Address.fromString(bitcoinjNetowrkParameters, address);
        Script script = ScriptBuilder.createOutputScript(addressBitcoinj);
        byte[] scriptArray = script.getProgram();
        byte[] scriptHash = Sha256Hash.hash(scriptArray);
        Sha256Hash reversedHash = Sha256Hash.wrapReversed(scriptHash);
        return reversedHash.toString();
    }

    static ECKey ECKeyFromWIF(String wif) {
        byte[] decodedWif = Base58.decode(wif);
        byte[] privateKey = Arrays.copyOfRange(decodedWif, 1, 33);
        return ECKey.fromPrivate(privateKey);
    }

    public static ECKey generateAndDumpAddress() {

        ECKey keypair = new ECKey();
        String wif = keypair.getPrivateKeyAsWiF(RegTestParams.get());
        String address = SegwitAddress.fromKey(RegTestParams.get(), keypair).toString();

        log.info("address: {}", address);
        log.info("wif: {}", wif);
        log.info("scripthash: {}", scripthash(RegTestParams.get(), address));

        return keypair;
    }
}
