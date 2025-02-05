package dev.kumru.bitcoin;

import dev.kumru.bitcoin.electrum.Electrum;
import dev.kumru.bitcoin.electrum.dto.BlockchainScripthashListUnspentResponseEntry;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
class BitcoinTransactionUtilsIntegrationTests {
    static String senderKeyWIF = "cPukWDfJjmSBkScXg5S7NuvfSEKHvqTp9Nn6Rw5kB1vqMD5zdF1c";
    static String senderP2WPKHAddress = "bcrt1q9wgy7hp3rc8vjmsqsqrlmxqgt9ax6akaejvue2";

    static String receiverP2WPKHAddress = "bcrt1qrvr0f6k8pg0mtklwyj0htq4rzt4vc8602z5zkt";
    static String receiverKeyWIF = "bcrt1qrvr0f6k8pg0mtklwyj0htq4rzt4vc8602z5zkt";

    @Test
    void should_build_p2wpkh_bitcoin_transaction() throws Throwable {
        ECKey senderKey = BitcoinUtils.ECKeyFromWIF(senderKeyWIF);
        Coin amount = Coin.ofBtc(BigDecimal.valueOf(2));
        Coin fee = Coin.ofBtc(BigDecimal.valueOf(0.00001));

        String rawTx = BitcoinTransactionUtils.BuildRawP2WPKHTransaction(amount, senderP2WPKHAddress, receiverP2WPKHAddress, senderKey, fee);

        log.info(rawTx);
    }

}