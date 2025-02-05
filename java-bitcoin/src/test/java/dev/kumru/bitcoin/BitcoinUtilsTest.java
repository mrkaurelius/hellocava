package dev.kumru.bitcoin;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.params.RegTestParams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class BitcoinUtilsTest {

    @Test
    void should_calculate_scripthash() {

        String testP2WPKHAddress = "bcrt1qx0c4xps6s694fhftum2hv6phsfelwx0r076ps9";
        String expetedScriptHash = "36732b7dda5bc8ac627c0061434328e903b5d2657d83013f69d21f780cbb84d3";

        assertEquals(BitcoinUtils.scripthash(RegTestParams.get(), testP2WPKHAddress), expetedScriptHash);
    }

    @Test
    void should_generate_account() {

        BitcoinUtils.generateAndDumpAddress();
    }

}