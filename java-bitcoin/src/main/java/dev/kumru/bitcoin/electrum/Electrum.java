package dev.kumru.bitcoin.electrum;

import com.google.common.reflect.TypeToken;
import com.googlecode.jsonrpc4j.JsonRpcClient;
import dev.kumru.bitcoin.BitcoinUtils;
import dev.kumru.bitcoin.electrum.dto.BlockchainScripthashListUnspentResponseEntry;
import dev.kumru.bitcoin.electrum.dto.BlockchainTransactionGetVerboseResponse;
import dev.kumru.bitcoin.electrum.dto.ScriptPubKey;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.RegTestParams;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;

/**
 * TODO proper impl.
 */
@Slf4j
public class Electrum {
    private final String electrumServer;
    private final Integer electrumPort;
    private final NetworkParameters networkParameters = RegTestParams.get();

    private JsonRpcClient client = new JsonRpcClient();

    public Electrum(String electrumServer, Integer electrumPort) {
        this.electrumServer = electrumServer;
        this.electrumPort = electrumPort;
    }

    /**
     * @param address bitcoin address in type of networkParameters
     * @return confirmed (minded) balance of address
     */
    public List<BlockchainScripthashListUnspentResponseEntry> getUnspents(String address) {
        String method = "blockchain.scripthash.listunspent";
        Object params = List.of(BitcoinUtils.scripthash(networkParameters, address));

        try (Socket clientSocket = new Socket(electrumServer, electrumPort)) {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = new AppendNewLineOutputStream(clientSocket.getOutputStream());

            Type returnType = new TypeToken<List<BlockchainScripthashListUnspentResponseEntry>>() {
            }.getType();
            List<BlockchainScripthashListUnspentResponseEntry> result = (List<BlockchainScripthashListUnspentResponseEntry>)
                    client.invokeAndReadResponse(method, params, returnType, outputStream, inputStream);

            return result;
        } catch (Throwable e) {
            throw new RuntimeException("getUnspent error", e);
        }
    }

    public BlockchainTransactionGetVerboseResponse getTransaction(String txHash) {
        String method = "blockchain.transaction.get";
        Object params = List.of(txHash, true);

        Type returnType = new TypeToken<BlockchainTransactionGetVerboseResponse>() {
        }.getType();

        return (BlockchainTransactionGetVerboseResponse) request(method, params, returnType);
    }

    public String getRawTransaction(String txHash) {
        String method = "blockchain.transaction.get";
        Object params = List.of(txHash, false);

        Type returnType = new TypeToken<String>() {
        }.getType();

        return (String) request(method, params, returnType);
    }


    /**
     * getScriptPubKey of an output
     */
    public ScriptPubKey getOutputScriptPubKey(String txHash, long txPosition) {
        BlockchainTransactionGetVerboseResponse tx = getTransaction(txHash);
        return tx.getOutputs().get((int) txPosition).getScriptPubKey(); // TODO is this casting safe?
    }

    /**
     * Verbatim from https://github.com/electrumj/electrumj
     * OutputStream wrapper that appends a '\n' char after each write invocation.
     * Electrumx expects a '\n' char after each request.
     */
    public static class AppendNewLineOutputStream extends FilterOutputStream {
        public AppendNewLineOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            super.write(b, off, len);
            write('\n');
        }
    }

    private Object request(String method, Object params, Type returnType) {
        try (Socket clientSocket = new Socket(electrumServer, electrumPort)) {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = new AppendNewLineOutputStream(clientSocket.getOutputStream());

            return client.invokeAndReadResponse(method, params, returnType, outputStream, inputStream);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String testAddres = "bcrt1qx0c4xps6s694fhftum2hv6phsfelwx0r076ps9";
        Electrum electrum = new Electrum("127.0.0.1", 60401);

    }
}