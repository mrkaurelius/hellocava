package dev.kumru.bitcoin.electrum;

import dev.kumru.bitcoin.electrum.dto.BlockchainScripthashListUnspentResponseEntry;
import dev.kumru.bitcoin.electrum.dto.ScriptPubKey;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

/**
 * Electrum integration tests
 */
@Slf4j
class ElectrumIntegrationTests {

    static String developmentKeyWIF = "cNQdYufLRboqCnTHEnyF61Z3ZvdQbi5YwhLzCtQnPW1nSLHmzJjZ";
    static String developmentP2WPKHAddress = "bcrt1qx0c4xps6s694fhftum2hv6phsfelwx0r076ps9";
    static String developmentScriptPubKey = "001433f153061a868b54dd2be6d57668378273f719e3";

    static Electrum electrum;

    @BeforeAll
    static void setup() {
        electrum = new Electrum("127.0.0.1", 60401);
    }

    @Test
    void should_get_output_script_pub_key() {
        List<BlockchainScripthashListUnspentResponseEntry> unspends = electrum.getUnspents(developmentP2WPKHAddress);
        log.info("unspent size: {}", unspends.size());

        unspends.sort(Comparator.comparingLong(BlockchainScripthashListUnspentResponseEntry::getHeight));
        BlockchainScripthashListUnspentResponseEntry first = unspends.getFirst();
        log.info("Getting scriptpubkey of output: {}", first.toString());

        String txHash = first.getTxHash();
        long txPos = first.getTxPos();

        ScriptPubKey scriptPubKey = electrum.getOutputScriptPubKey(txHash, txPos);
        log.info(scriptPubKey.toString());
    }

    /**
     * Get transaction of an unspent
     */
    @Test
    void should_get_raw_transaction() {
        List<BlockchainScripthashListUnspentResponseEntry> unspends = electrum.getUnspents(developmentP2WPKHAddress);
        log.info("unspent size: {}", unspends.size());

        unspends.sort(Comparator.comparingLong(BlockchainScripthashListUnspentResponseEntry::getHeight));
        BlockchainScripthashListUnspentResponseEntry first = unspends.getFirst();

        String txHash = first.getTxHash();

        String rawTxHex = electrum.getRawTransaction(txHash);
        log.info(rawTxHex);
    }
}