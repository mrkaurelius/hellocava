package dev.kumru.bitcoin.electrum.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * The ScriptPubKey is the locking code for an output.
 * It's made up of Script, which is a mini programming language that allows you to place different types of locks on your outputs.
 * Most ScriptPubKeys use one of a standard set of locks, most commonly where the output is locked to a public key (e.g. P2PKH or P2WPKH).
 */
@Data
@ToString
public class ScriptPubKey {
    private String address;
    private String asm;
    private String desc;
    private String hex;
    private String type;
}