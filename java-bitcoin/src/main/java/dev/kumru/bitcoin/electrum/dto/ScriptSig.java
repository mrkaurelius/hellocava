package dev.kumru.bitcoin.electrum.dto;

import lombok.Data;
import lombok.ToString;


/**
 * A ScriptSig provides the unlocking code for a previous output.
 */
@Data
@ToString
public class ScriptSig {
    private String asm;
    private String hex;
}