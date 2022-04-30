// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client;

import engine.net.AbstractConnection;
import engine.server.MBServerStatics;
import engine.util.ByteUtils;
import org.pmw.tinylog.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.*;

public class ClientAuthenticator {

    private final AbstractConnection origin;

    private ByteBuffer buffer = ByteBuffer.allocate(100);
    private byte[] secretKeyBytes = new byte[16];
    private SecretKeySpec BFKey;

    private Cipher cipher;
    private byte[] iVecEnc = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private byte[] iVecDec = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private int iVecEncOffset = 0;
    private int iVecDecOffset = 0;
    private int totalRead = 0;
    private KeyFactory keyFactory;
    private DHParameterSpec dhParamSpec;
    private KeyPairGenerator keyPairGen;
    private KeyAgreement keyAgree;
    private boolean initialized = false;
    private boolean keyInit = false;
    private byte[] secretKey;
    private byte[] serverPublicKey;

    public ClientAuthenticator(AbstractConnection origin) {
        super();
        this.origin = origin;
        try {
            // init the resuable Crypto Stuff.
            this.keyFactory = KeyFactory.getInstance("DH");
            this.dhParamSpec = new DHParameterSpec(ClientAuthenticator.P, ClientAuthenticator.G);

            this.keyPairGen = KeyPairGenerator.getInstance("DH");
            this.keyPairGen.initialize(this.dhParamSpec);
            this.keyAgree = KeyAgreement.getInstance("DH");

        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            Logger.error("NoSuchAlgorithmException  " + e.getMessage());
            this.keyInit = false;
            return;
        }

        this.keyInit = true;
    }

    private void calcKeys(AbstractConnection origin, byte[] clientPublicKeyBytes) {

        try {
			// get the forwarded client public key, in byte[] form.

            // Convert client public key to a BigInteger
            BigInteger clientPublicKeyBI = new BigInteger(1, clientPublicKeyBytes);

            // convert the client's Public Key to a DHPublicKey object
            DHPublicKeySpec dhKeySpec = new DHPublicKeySpec(clientPublicKeyBI, ClientAuthenticator.P, ClientAuthenticator.G);
            DHPublicKey clientPublicKey = (DHPublicKey) this.keyFactory.generatePublic(dhKeySpec);

            // Now calculate the server's PublicKey
            byte[] serverPublicKeyBytes = new byte[96];
            boolean invalid = true;

            int tryCnt = 1;
            while (invalid) {
                KeyPair keyPair = keyPairGen.generateKeyPair();
                this.keyAgree = KeyAgreement.getInstance("DH");
                this.keyAgree.init(keyPair.getPrivate());
                DHPublicKey serverPublicKey = (DHPublicKey) keyPair.getPublic();

                String hex = serverPublicKey.getY().toString(16);

                if (hex.length() == 192) {
                    invalid = false;
                    serverPublicKeyBytes = ByteUtils.stringHexToByteArray(hex);
                }
                if (tryCnt >= 5)
                    // Give java 4 tries to get a Public key of valid length.
                    throw new Exception("Not able to generate a valid length public key");
                ++tryCnt;
            }

            // Calculate shared DH Secret Key
            keyAgree.doPhase(clientPublicKey, true);
            this.secretKey = keyAgree.generateSecret();
            this.serverPublicKey = serverPublicKeyBytes;

            // Now we have the server's publicKey and the common secretKey
        } catch (Exception e) {
            origin.disconnect();
            Logger.error(e);
        }

    }

    public synchronized int initialize(AbstractConnection origin) {
        long startTime = System.currentTimeMillis();
        int read = -1;

        // Read the data from connections socket channel
        try {
            read = origin.getSocketChannel().read(this.buffer);
        } catch (IOException e) {
        	if (e.getLocalizedMessage() != null && !e.getLocalizedMessage().equals(MBServerStatics.EXISTING_CONNECTION_CLOSED) && !e.getLocalizedMessage().equals(MBServerStatics.RESET_BY_PEER))
        		  Logger.error(e);
                  origin.disconnect();
        	return 0;
        }

        if (read == -1) {
        	 Logger.info("EOF on Socket Channel, Disconnecting " + origin.getLocalAddressAndPortAsString());
            origin.disconnect();
            return read;
        }

        this.totalRead += read;

        if (this.totalRead > 100)
            Logger.error( "Possible Spam warning: "
                    + origin.getSocketChannel().socket().toString());

        // Not all arrived yet, so wait for more
        if (this.totalRead < 100)
            return read;

        this.buffer.flip();
        this.buffer.getInt(); // get the length first & throw away value.

        byte[] PeerPubKeyEnc = new byte[96];
        this.buffer.get(PeerPubKeyEnc);

        this.calcKeys(origin, PeerPubKeyEnc);

        try {
            byte[] sharedSecret = this.secretKey;
            byte[] PubKeyEnc = this.serverPublicKey;

            // Cut DH SecretKey down to 16 bytes
            System.arraycopy(sharedSecret, 0, secretKeyBytes, 0, 16);

            // Calculate Blowfish Secret Key and make ciphers streams.
            this.BFKey = new SecretKeySpec(this.secretKeyBytes, "Blowfish");

			// Initialize cipher and Ivecs.
            // Ivecs must be run through the cipher once
            // to prep the cipher for cfb mode.
            this.cipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
            this.cipher.init(Cipher.ENCRYPT_MODE, this.BFKey);
            this.cipher.update(this.iVecEnc, 0, 8, this.iVecEnc, 0);
            this.cipher.update(this.iVecDec, 0, 8, this.iVecDec, 0);

            // Send public key to peer
            byte[] pubKeyLen = {0x00, 0x00, 0x00, 0x60}; // hex for 96
            ByteBuffer bb = ByteBuffer.wrap(pubKeyLen);
            bb.position(bb.limit());
            origin.sendBB(bb);
            bb = ByteBuffer.wrap(PubKeyEnc);
            bb.position(bb.limit());
            origin.sendBB(bb);
        } catch (Exception e) {
            Logger.error(e);
            origin.disconnect();
            return read;
        }
        /*
         * //Send Secret Key to Login Server if (PortalServer.ServerType ==
         * "Login") { byte[] keyinfo = new byte[20]; keyinfo[0] = 0x11;
         * keyinfo[1] = 0x11; keyinfo[2] = 0x11; keyinfo[3] = 0x11; for (int i =
         * 0; i < 16; i++) keyinfo[i + 4] = ShortSharedSecret[i];
         * PortalPair.HandleOutput(keyinfo, 20); }
         */
        this.initialized = true;

//        long endTime = System.currentTimeMillis();
//
//        long time = endTime - startTime;
//       // Logger.debug("", "Authenticator took " + time + " ms to initialize.");

        return read;
    }

    public synchronized void encrypt(final ByteBuffer dataIn, final ByteBuffer dataOut) {
        try {
            // Assume that if position != 0 that we need to flip.
            if (dataIn.position() != 0)
                dataIn.flip();

            int count = dataIn.limit();

            //Line up the iVecEncOffset.. fall through is intentional
            if (iVecEncOffset != 0)
                if ((this.iVecEncOffset + dataIn.limit()) < 8) {
					//This handles cases where the net msg + offset won't reach 8 bytes total
                    //prevents BufferUnderflowException in small net messages. -
                    int newEncOffset = this.iVecEncOffset + dataIn.limit();
                    for (int i = this.iVecEncOffset; i < newEncOffset; i++) {
                        this.iVecEnc[i] = (byte) (dataIn.get() ^ this.iVecEnc[i]);
                        dataOut.put(this.iVecEnc[i]);
                    }
                    this.iVecEncOffset = newEncOffset;
                    return;
                } else
                    switch (iVecEncOffset) {
                        case 1:
                            this.iVecEnc[1] = (byte) (dataIn.get() ^ this.iVecEnc[1]);
                            dataOut.put(this.iVecEnc[1]);
                        case 2:
                            this.iVecEnc[2] = (byte) (dataIn.get() ^ this.iVecEnc[2]);
                            dataOut.put(this.iVecEnc[2]);
                        case 3:
                            this.iVecEnc[3] = (byte) (dataIn.get() ^ this.iVecEnc[3]);
                            dataOut.put(this.iVecEnc[3]);
                        case 4:
                            this.iVecEnc[4] = (byte) (dataIn.get() ^ this.iVecEnc[4]);
                            dataOut.put(this.iVecEnc[4]);
                        case 5:
                            this.iVecEnc[5] = (byte) (dataIn.get() ^ this.iVecEnc[5]);
                            dataOut.put(this.iVecEnc[5]);
                        case 6:
                            this.iVecEnc[6] = (byte) (dataIn.get() ^ this.iVecEnc[6]);
                            dataOut.put(this.iVecEnc[6]);
                        case 7:
                            this.iVecEnc[7] = (byte) (dataIn.get() ^ this.iVecEnc[7]);
                            dataOut.put(this.iVecEnc[7]);
                            count -= (8 - iVecEncOffset);
                            this.iVecEncOffset = 0;
                            this.cipher.update(this.iVecEnc, 0, 8, this.iVecEnc, 0);
                    }

            //Main loop - unrolled x8
            int loopCount = (count) >> 3;
            for (int i = 0; i < loopCount; i++) {

                this.iVecEnc[0] = (byte) (dataIn.get() ^ this.iVecEnc[0]);
                this.iVecEnc[1] = (byte) (dataIn.get() ^ this.iVecEnc[1]);
                this.iVecEnc[2] = (byte) (dataIn.get() ^ this.iVecEnc[2]);
                this.iVecEnc[3] = (byte) (dataIn.get() ^ this.iVecEnc[3]);
                this.iVecEnc[4] = (byte) (dataIn.get() ^ this.iVecEnc[4]);
                this.iVecEnc[5] = (byte) (dataIn.get() ^ this.iVecEnc[5]);
                this.iVecEnc[6] = (byte) (dataIn.get() ^ this.iVecEnc[6]);
                this.iVecEnc[7] = (byte) (dataIn.get() ^ this.iVecEnc[7]);

                dataOut.put(this.iVecEnc[0]);
                dataOut.put(this.iVecEnc[1]);
                dataOut.put(this.iVecEnc[2]);
                dataOut.put(this.iVecEnc[3]);
                dataOut.put(this.iVecEnc[4]);
                dataOut.put(this.iVecEnc[5]);
                dataOut.put(this.iVecEnc[6]);
                dataOut.put(this.iVecEnc[7]);
                this.cipher.update(this.iVecEnc, 0, 8, this.iVecEnc, 0);
            }

            //Resync the iVecEncOffset to handle the remainder..
            this.iVecEncOffset = count % 8;
            for (int i = 0; i < iVecEncOffset; i++) {
                this.iVecEnc[i] = (byte) (dataIn.get() ^ this.iVecEnc[i]);
                dataOut.put(this.iVecEnc[i]);
            }
        } catch (BufferUnderflowException e) {
            if (dataIn != null && dataOut != null)
                Logger.warn("Encrypt Error: (in)" + dataIn.toString() + " :: (out)" + dataOut.toString());
            Logger.error("ClientAuth.encrypt() -> Underflow" + e);
        } catch (BufferOverflowException e) {
            if (dataIn != null && dataOut != null)
                Logger.warn("Encrypt Error: (in)" + dataIn.toString() + " :: (out)" + dataOut.toString());
            Logger.error("ClientAuth.encrypt() -> Overflow" + e);
        } catch (Exception e) {
            Logger.error("ClientAuth.encrypt()" + e);
        }
    }

    public synchronized void decrypt(ByteBuffer dataIn, ByteBuffer dataOut) {
        try { // get lock
            synchronized (dataIn) { // TODO is this lock needed?

                // Assume that if position != 0 that we need to flip.
                if (dataIn.position() != 0)
                    dataIn.flip();

                byte encryptedByte;
                byte decryptedByte;

                for (int i = 0; i < dataIn.limit(); ++i) {

                    // Get byte out of ByteBuffer
                    encryptedByte = dataIn.get();

                    // XOR it against the iVEC
                    decryptedByte = (byte) (encryptedByte ^ this.iVecDec[this.iVecDecOffset]);

                    // put the decrypted byte into the outgoing ByteBuffer
                    dataOut.put(decryptedByte);

                    // store the encrypted byte back into the iVEC
                    this.iVecDec[this.iVecDecOffset] = encryptedByte;

                    // Increment ivecOffset
                    this.iVecDecOffset++;

                    // Check to see if iVecOffset is at MAX. If so, reset
                    if (this.iVecDecOffset > 7) {
                        try {
                            this.cipher.update(this.iVecDec, 0, 8,
                                    this.iVecDec, 0);
                        } catch (ShortBufferException e) {
                            // suck up this error
                            Logger.error(e);
                        }
                        this.iVecDecOffset = 0;
                    }
                }
               
               
            }
        } catch (Exception e) {
            Logger.error("ClientAuth.decrypt()" + e);
        }
    }

    private void initiateKey(byte[] clientPublicKeyBytes) {

    }

    /**
     * @return the secretKeyBytes
     */
    public byte[] getSecretKeyBytes() {
        return secretKeyBytes;
    }

    /**
     * @return the initialized
     */
    public boolean initialized() {
        return initialized;
    }

    private static final byte P_Bytes[] = {(byte) 0xFB, (byte) 0x46, (byte) 0x56, (byte) 0xB4, (byte) 0xBE, (byte) 0x81, (byte) 0xA4,
        (byte) 0x2C, (byte) 0x37, (byte) 0xC4, (byte) 0xA2, (byte) 0x61, (byte) 0x4A, (byte) 0xAC, (byte) 0x65, (byte) 0x90,
        (byte) 0x31, (byte) 0xB6, (byte) 0x83, (byte) 0x26, (byte) 0x63, (byte) 0x94, (byte) 0x08, (byte) 0x95, (byte) 0x56,
        (byte) 0x8D, (byte) 0x5E, (byte) 0xBF, (byte) 0x94, (byte) 0x10, (byte) 0x5A, (byte) 0x37, (byte) 0xB6, (byte) 0x82,
        (byte) 0x1A, (byte) 0x75, (byte) 0x2B, (byte) 0xF1, (byte) 0x94, (byte) 0xB7, (byte) 0x7E, (byte) 0x56, (byte) 0xC6,
        (byte) 0xD1, (byte) 0xF5, (byte) 0x18, (byte) 0xE1, (byte) 0xA5, (byte) 0x13, (byte) 0x9E, (byte) 0xC1, (byte) 0x85,
        (byte) 0x98, (byte) 0xB7, (byte) 0x32, (byte) 0xDB, (byte) 0x38, (byte) 0x09, (byte) 0x1A, (byte) 0xF8, (byte) 0x5C,
        (byte) 0xDA, (byte) 0x4F, (byte) 0x9F, (byte) 0x67, (byte) 0x93, (byte) 0x72, (byte) 0x8F, (byte) 0x75, (byte) 0x4F,
        (byte) 0x0B, (byte) 0xBD, (byte) 0x69, (byte) 0x61, (byte) 0x97, (byte) 0x1F, (byte) 0xEE, (byte) 0xFB, (byte) 0x5B,
        (byte) 0xB0, (byte) 0x85, (byte) 0xC4, (byte) 0x27, (byte) 0x7E, (byte) 0x41, (byte) 0x42, (byte) 0xC2, (byte) 0xF1,
        (byte) 0xDA, (byte) 0x64, (byte) 0x8F, (byte) 0x4E, (byte) 0x28, (byte) 0xFD, (byte) 0x2A, (byte) 0x63};

    private static final BigInteger P = new BigInteger(1, P_Bytes);
    private static final BigInteger G = BigInteger.valueOf(5);

}
