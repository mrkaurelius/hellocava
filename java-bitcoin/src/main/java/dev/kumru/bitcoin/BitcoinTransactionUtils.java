package dev.kumru.bitcoin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.kumru.bitcoin.electrum.Electrum;
import dev.kumru.bitcoin.electrum.dto.BlockchainScripthashListUnspentResponseEntry;
import dev.kumru.bitcoin.electrum.dto.BlockchainTransactionGetVerboseResponse;
import dev.kumru.bitcoin.electrum.dto.TxOutput;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/**
 * Bitcoin transaction util
 * <p>
 * Objectives
 * - Generate a bitcoin transaction which have unlocked (signed) inputs and locked (signed) outputs
 */
@Slf4j
public class BitcoinTransactionUtils {
    public static NetworkParameters networkParameters = RegTestParams.get();

    /*
     * Eli bol transaction
     */
    public static String BuildRawP2WPKHTransaction(Coin amount, String senderAddress, String receiverAddress, ECKey senderKey, Coin fee) {
        Electrum electrum = new Electrum("127.0.0.1", 60401);

        // Our transaction
        Transaction p2wpkhTransaction = new Transaction(RegTestParams.get());
        log.info("hasWitnesses: {}", p2wpkhTransaction.hasWitnesses());

        List<BlockchainScripthashListUnspentResponseEntry> unspends = electrum.getUnspents(senderAddress);
        unspends.sort(Comparator.comparingLong(BlockchainScripthashListUnspentResponseEntry::getHeight));

        List<BlockchainScripthashListUnspentResponseEntry> unspendsToSpend = new ArrayList<BlockchainScripthashListUnspentResponseEntry>();

        // TODO daha iyi isimler

        long inputAmountSat = amount.add((fee)).toSat();
        long unspendAmount = 0L;
        for (BlockchainScripthashListUnspentResponseEntry unspend : unspends) {
            unspendAmount += unspend.getValue();
            unspendsToSpend.add(unspend);


            if (unspendAmount >= inputAmountSat) {
                break;
            }
        }

        if (unspendAmount < inputAmountSat) {
            log.error("Insufficient balance, {}, {}", unspendAmount, inputAmountSat);
            throw new RuntimeException("Insufficient balance");
        }

        /*
         * Adding output to transaction
         */

        p2wpkhTransaction.addOutput(amount, Address.fromString(RegTestParams.get(), receiverAddress));

        /*
         * Adding inputs to transaction
         */

        for (BlockchainScripthashListUnspentResponseEntry unspend : unspendsToSpend) {
            // TODO from tx olmadan yapmak mumkun mu?
            Coin unspendValue = Coin.valueOf(unspend.getValue());
            TransactionOutPoint outPoint = new TransactionOutPoint(RegTestParams.get(), unspend.getTxPos(), Sha256Hash.wrap(unspend.getTxHash()));

            TransactionInput input = new TransactionInput(RegTestParams.get(), null, new byte[0], outPoint, unspendValue);
            p2wpkhTransaction.addInput(input);
        }


        log.info(">>> transaction: {}", p2wpkhTransaction);
        log.info(">>> transactionHex: {}", p2wpkhTransaction.toHexString());

        /**
         * Calculate change for fee
         */

        Coin inputSum = p2wpkhTransaction.getInputSum();
        Coin outputSum = p2wpkhTransaction.getOutputSum();

        Coin diff = inputSum.minus(outputSum);
        log.info("diff: {}", diff);
        Coin change = diff.minus(fee);
        log.info("change: {}", change);

        p2wpkhTransaction.addOutput(change, Address.fromString(RegTestParams.get(), senderAddress));

        /*
         * Signing inputs
         */
        for (TransactionInput input : p2wpkhTransaction.getInputs()) {
            Script witnessScript = ScriptBuilder.createP2PKHOutputScript(senderKey);
            Sha256Hash hash = p2wpkhTransaction.hashForWitnessSignature(input.getIndex(), witnessScript, input.getValue(), Transaction.SigHash.ALL, false);

            // TODO offline signing
            ECKey.ECDSASignature signature = senderKey.sign(hash);
            BigInteger r = signature.r;
            BigInteger s = signature.s;
            TransactionSignature transactionSignature = new TransactionSignature(r, s);

            TransactionWitness witness = TransactionWitness.redeemP2WPKH(transactionSignature, senderKey);
            input.setWitness(witness);

            log.info("input witness hash: {}", hash);

            /*
             * Bitcoinj ECKey signing
             */
//            TransactionSignature transactionSignature = p2wpkhTransaction.calculateWitnessSignature(input.getIndex(), senderKey, witnessScript, input.getValue(), Transaction.SigHash.ALL, false);
//            TransactionWitness witness = TransactionWitness.redeemP2WPKH(transactionSignature, senderKey);
//            input.setWitness(witness);
        }

        log.info("p2wpkhTransaction: {}", p2wpkhTransaction);

        return p2wpkhTransaction.toHexString();
    }

    /*
     * Build transaction with specific inputs
     */
    public static String BuildRawP2WPKHTransaction(long amount, String senderAddress, String receiverAddress, ECKey senderKey, List<BlockchainScripthashListUnspentResponseEntry> unspents) {
        Electrum electrum = new Electrum("127.0.0.1", 60401);

        // Our transaction
        Transaction p2wpkhTransaction = new Transaction(RegTestParams.get());

        p2wpkhTransaction.addOutput(Coin.valueOf(amount), Address.fromString(RegTestParams.get(), receiverAddress));

        // Output script for
        Script outputScript = ScriptBuilder.createP2WPKHOutputScript(senderKey);

        for (BlockchainScripthashListUnspentResponseEntry unspend : unspents) {
            String txHash = unspend.getTxHash();
            String fromTxHex = electrum.getRawTransaction(txHash);
            Transaction fromTx = new Transaction(RegTestParams.get(), HexFormat.of().parseHex(fromTxHex));

            TransactionOutPoint outPoint = new TransactionOutPoint(RegTestParams.get(), unspend.getTxPos(), fromTx);
//            TransactionInput input = tx.addSignedInput(outPoint, ScriptBuilder.createOutputScript(fromAddress), inAmount, fromKey);

            p2wpkhTransaction.addSignedInput(outPoint, ScriptBuilder.createOutputScript(Address.fromString(RegTestParams.get(), senderAddress)), Coin.valueOf(unspend.getValue()), senderKey);
        }

        log.info("p2wpkhTransaction: {}", p2wpkhTransaction);

        return p2wpkhTransaction.toHexString();
    }

    /*
     * TODO tum unspentleri alma, sadece bir kismini al
     */
    public static String E2EP2WPKHTransaction(ECKey senderKey, String senderaddress, String receiverAddress) throws Throwable {
        Electrum electrum = new Electrum("127.0.0.1", 60401);
        List<BlockchainScripthashListUnspentResponseEntry> unspents = electrum.getUnspents(senderaddress);

        log.info("unspents lenght: {}", unspents.size());
        unspents.sort(Comparator.comparingLong(BlockchainScripthashListUnspentResponseEntry::getHeight));

        // Get oldest unspentToSpend transaction
        BlockchainScripthashListUnspentResponseEntry unspentToSpend = unspents.getFirst();
        log.info("unspentToSpend: {}", unspentToSpend);
        Pretty.print(unspentToSpend);

        String parentTransactionHash = "eb79181f188c2eee777f6cbfb524334b5942fe2aace8f4cdc46a8affd925a740";
        Integer outputPosition = 1;

        BlockchainTransactionGetVerboseResponse parentTx = electrum.getTransaction(parentTransactionHash);
        log.info("parent of unspentToSpend: {}", parentTx);
        Pretty.print(parentTx);

        TxOutput txOutput = parentTx.getOutputs().get((int) unspentToSpend.getTxPos());
        log.info("txOutput: {}", txOutput);
        Pretty.print(txOutput);

        /*
         * An output is a data structure specifying a value and a script, conventionally called the scriptPubKey
         *
         * Bitcoinj script == scriptPubKey
         */
        Transaction parentTransaction = new Transaction(RegTestParams.get(), HexFormat.of().parseHex(parentTx.getHex()));
        log.info("parentTransaction: {}", parentTransaction);
        TransactionOutput outputToSpent = parentTransaction.getOutputs().get(outputPosition);
        log.info("outputToSpent: {}", outputToSpent);

        /*
         * Offline transaction generation
         */
        Transaction offlineTransaction = new Transaction(RegTestParams.get());
        log.info("offlineTransaction: {}", offlineTransaction);

        Coin transactionAmount = Coin.valueOf(9900000);
        offlineTransaction.addOutput(transactionAmount, Address.fromString(RegTestParams.get(), receiverAddress));

        TransactionOutPoint outPoint = new TransactionOutPoint(RegTestParams.get(), 1, parentTransaction);
        Script inputScript = ScriptBuilder.createP2WPKHOutputScript(senderKey);

        Coin value = Coin.valueOf(10000000L);
        offlineTransaction.addSignedInput(outPoint, inputScript, value, senderKey);

        log.info("offlineTransaction: {}", offlineTransaction);
        log.info("output sum: {}", offlineTransaction.getOutputSum());
        log.info("input sum: {}", offlineTransaction.getInputSum());

        String transactionHexString = offlineTransaction.toHexString();
        log.info(transactionHexString);

        return transactionHexString;
    }

    record BitcoinOutput(String txid, Long n, Long value) {
    }

    /*
     * Outputs to spend
     */
    record BitcoinOutputPayload(TxOutput output, Transaction fromTx) {
    }

    class Pretty {
        private static ObjectMapper objectMapper;

        static {
            objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        @SneakyThrows
        public static void print(Object o) {
            System.out.println(objectMapper.writeValueAsString(o));
        }

    }
}
