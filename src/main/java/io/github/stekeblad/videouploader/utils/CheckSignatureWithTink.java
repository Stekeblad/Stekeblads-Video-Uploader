package io.github.stekeblad.videouploader.utils;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.config.TinkConfig;

/**
 * Allows checking digital signatures using Google Tink: https://github.com/google/tink/
 */
public class CheckSignatureWithTink {
    private static boolean isTinkRegistered = false;

    /**
     * Takes a byte array of data, and a byte array containing the signature produced for that data and
     * attempts to verify that the provided data and signature validates with the public key bundled in the program.
     * If the signature is not correct or was not produced by the private key that matches the bundled public key
     * then false is returned. If the signature was verified to being created for the provided data by the correct
     * private key then true is returned.
     *
     * @param data      the data that has been signed
     * @param signature the signature for the provided data
     * @return true if the signature + data + public key combo is valid, false otherwise
     */
    public static boolean verifySignature(byte[] data, byte[] signature) {
        try {
            if (!isTinkRegistered) {
                TinkConfig.register();
                isTinkRegistered = true;
            }

            KeysetHandle publicKey = CleartextKeysetHandle.read(JsonKeysetReader.withInputStream(
                    CheckSignatureWithTink.class.getClassLoader().getResourceAsStream("crypto/publicKey.pub")));

            PublicKeyVerify verifier = publicKey.getPrimitive(PublicKeyVerify.class);
            verifier.verify(signature, data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
