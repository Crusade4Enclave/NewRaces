// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net;

import engine.pooling.MultisizeByteBufferPool;

import java.nio.ByteBuffer;

public class Network {

    public static final int INITIAL_SOCKET_BUFFER_SIZE = 128 * 1024;
    public static final int INITIAL_BYTEBUFFER_POOL_SIZE = 256;

    public static final MultisizeByteBufferPool byteBufferPool = new MultisizeByteBufferPool();

    public static void init() {
		//Force a few to be created.

        //Small (2^10-15)
        for (int a = 10; a < 16; ++a) {
            for (int i = 0; i < 50; ++i) {
                byteBufferPool.putBuffer(ByteBuffer.allocateDirect(MultisizeByteBufferPool.powersOfTwo[a]));
            }
        }

        //standard size (2^16)
        for (int i = 0; i < 100; ++i) {
            byteBufferPool.putBuffer(ByteBuffer.allocateDirect(MultisizeByteBufferPool.powersOfTwo[16]));
        }

        //Large (2^17)
        for (int i = 0; i < 50; ++i) {
            byteBufferPool.putBuffer(ByteBuffer.allocateDirect(MultisizeByteBufferPool.powersOfTwo[17]));
        }

        // NetMsgFactory size (2^18)
        for (int i = 0; i < 64; ++i) {
            byteBufferPool.putBuffer(ByteBuffer
                    .allocateDirect(MultisizeByteBufferPool.powersOfTwo[18]));
        }

        //Very Large (2^19)
        for (int i = 0; i < 25; ++i) {
            byteBufferPool.putBuffer(ByteBuffer.allocateDirect(MultisizeByteBufferPool.powersOfTwo[19]));
        }

        //Very Large (2^20)
        for (int i = 0; i < 10; ++i) {
            byteBufferPool.putBuffer(ByteBuffer.allocateDirect(MultisizeByteBufferPool.powersOfTwo[20]));
        }
    }

}
