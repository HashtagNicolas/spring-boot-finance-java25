package com.hashtag.ngo.example.bank.bean.impl;

import com.hashtag.ngo.example.bank.bean.KeyDerivationService;
import org.springframework.stereotype.Service;

import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.HKDFParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * Derivation de cle via l'API KDF (JEP 510, "Key Derivation Function API",
 * finalisee en Java 25) : avant cette API, il n'existait pas de moyen
 * standard/portable de deriver une cle a partir d'un secret (HKDF, etc.) sans
 * passer par des primitives bas niveau (Mac manuel, ou bibliotheques tierces).
 * javax.crypto.KDF fournit une API uniforme (comme Cipher/Mac/Signature) pour
 * ces algorithmes de derivation.
 * <p>
 * Ici on utilise HKDF-SHA256 (RFC 5869) en un seul appel "extract-then-expand" :
 * on combine le secret maitre (IKM) et un sel pour obtenir une cle
 * pseudo-aleatoire, puis on l'etend a la longueur voulue en y liant une
 * information de contexte (evite qu'une meme paire secret/sel ne produise la
 * meme cle pour deux usages differents).
 */
@Service
public class KeyDerivationServiceImpl implements KeyDerivationService {

    private static final String HKDF_ALGORITHM = "HKDF-SHA256";
    private static final String DERIVED_KEY_ALGORITHM = "AES";
    private static final byte[] CONTEXT_INFO = "bank-api-account-encryption".getBytes(StandardCharsets.UTF_8);

    @Override
    public SecretKey deriveKey(byte[] masterSecret, byte[] salt, int keyLengthBytes) {
        try {
            KDF hkdf = KDF.getInstance(HKDF_ALGORITHM);
            HKDFParameterSpec spec = HKDFParameterSpec.ofExtract()
                    .addIKM(masterSecret)
                    .addSalt(salt)
                    .thenExpand(CONTEXT_INFO, keyLengthBytes);
            return hkdf.deriveKey(DERIVED_KEY_ALGORITHM, spec);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Echec de la derivation de cle (HKDF)", e);
        }
    }
}
