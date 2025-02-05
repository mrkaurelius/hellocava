package dev.kumru.bitcoin;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.HexFormat;

@Slf4j
public class BitcoinjPlayground {
    static String developmentKeyWIF = "cNQdYufLRboqCnTHEnyF61Z3ZvdQbi5YwhLzCtQnPW1nSLHmzJjZ";
    static String developmentP2WPKHAddress = "bcrt1qx0c4xps6s694fhftum2hv6phsfelwx0r076ps9";
    static String developmentScriptPubKey = "001433f153061a868b54dd2be6d57668378273f719e3";

    static HexFormat hexFormat = HexFormat.of();

    static PeerGroup peerGroup;
    static Wallet wallet;


    public static void main(String[] args) {
        try {
            DownloanBlockchain();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            peerGroup.stop();
        }
    }

    static void DownloanBlockchain() throws BlockStoreException, IOException {
        ECKey key = BitcoinUtils.ECKeyFromWIF(developmentKeyWIF);
        SegwitAddress address = SegwitAddress.fromKey(RegTestParams.get(), key);

        wallet = Wallet.createBasic(RegTestParams.get());
        wallet.importKey(key);


        NetworkParameters regtestParams = RegTestParams.get();
        BlockStore blockStore = new MemoryBlockStore(regtestParams);
        BlockChain chain = new BlockChain(regtestParams, wallet, blockStore);

        peerGroup = new PeerGroup(regtestParams, chain);

        PeerAddress peerAddress = new PeerAddress(RegTestParams.get(), InetAddress.getLocalHost(), 18444);
        peerGroup.addAddress(peerAddress, 29); // Priority value?
        peerGroup.start();

//        wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
//            @Override
//            public synchronized void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
//            }
//        });

        peerGroup.downloadBlockChain();

        String receiverP2WPKHAddress = "bcrt1qrvr0f6k8pg0mtklwyj0htq4rzt4vc8602z5zkt";
        String receiverKeyWIF = "bcrt1qrvr0f6k8pg0mtklwyj0htq4rzt4vc8602z5zkt";


        log.info("wallet balance: {}", wallet.getBalance());
        SendRequest sendRequest = SendRequest.to(SegwitAddress.fromString(RegTestParams.get(), receiverP2WPKHAddress), Coin.ofBtc(BigDecimal.valueOf(0.1)));
        try {
            Wallet.SendResult sendResult = wallet.sendCoins(peerGroup, sendRequest);
            log.info("sendResult: {}", sendResult);
        } catch (InsufficientMoneyException e) {
            throw new RuntimeException(e);
        }
    }

}
