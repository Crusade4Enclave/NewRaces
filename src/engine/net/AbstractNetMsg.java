// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net;

import engine.exception.SerializationException;
import engine.net.client.Protocol;
import engine.server.MBServerStatics;
import engine.util.StringUtils;
import org.pmw.tinylog.Logger;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents the NetMsgs set to/from the SBClient and in between
 * MBServer Server Suite components. Note that since the NetMsgs sent to/from
 * the SBClient do NOT include a MsgLen or DataLen parameter, special
 * serialization/deserialization must be implemented.
 *
 */
public abstract class AbstractNetMsg {

    protected final Protocol protocolMsg;
    private AbstractConnection origin;

    private static ConcurrentHashMap<Protocol, NetMsgStat> stats = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_HIGH);

    /**
     * This is the general purpose constructor.
     *
     * @param protocolMsg
     */
    protected AbstractNetMsg(Protocol protocolMsg) {
        super();
        this.protocolMsg = protocolMsg;
    }

    protected AbstractNetMsg(Protocol protocolMsg, AbstractConnection origin) {
        super();
        this.protocolMsg = protocolMsg;
        this.origin = origin;
    }

    protected AbstractNetMsg(Protocol protocolMsg, AbstractNetMsg msg) {
        super();
        this.protocolMsg = protocolMsg;
        this.origin = msg.origin;
    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     *
     * @param reader
     */
    protected AbstractNetMsg(Protocol protocolMsg, AbstractConnection origin,
                             ByteBufferReader reader)
             {
        this.protocolMsg = protocolMsg;
        this.origin = origin;

        // Call the subclass specific deserializer
        try {
            this._deserialize(reader);
        } catch (NullPointerException e) {
            Logger.error(e);
        }
    }

    /**
     * Deserializes the subclass specific items from the supplied
     * ByteBufferReader
     *
     * @param reader
     */
    protected abstract void _deserialize(ByteBufferReader reader);

    /**
     * Serializes the subclass specific items to the supplied ByteBufferWriter
     *
     * @param writer
     * @throws Exception
     */
    protected abstract void _serialize(ByteBufferWriter writer)
            throws SerializationException;

    /**
     * Attempts to serialize this NetMsg into a ByteBuffer. ByteBuffer is
     * obtained from a pool, so to retain max efficiency, the caller needs to
     * return this BB to the pool. Header size and layout is entirely defined by
     * the subclass of AbstractNetMsg
     *
     * @return a ByteBuffer
     */
    public final ByteBuffer serialize() {

        NetMsgStat stat;

        if (!AbstractNetMsg.stats.containsKey(this.protocolMsg)) {
            stat = new NetMsgStat(this.protocolMsg, this.getPowerOfTwoBufferSize());
            AbstractNetMsg.stats.put(this.protocolMsg, stat);
        } else
            stat = AbstractNetMsg.stats.get(this.protocolMsg);
        int lowerPow = stat.getMax();
        int upperPow = lowerPow + 4;

        ByteBuffer bb = null;
        
        int startPos = 0;
        ByteBufferWriter writer = null;

        for (int i = lowerPow; i < upperPow; ++i) {

            // get an appropriate sized BB from pool

            bb = Network.byteBufferPool.getBuffer(i);

            // Mark start position

            startPos = bb.position();

            // Make a writer

            writer = new ByteBufferWriter(bb); // FIXME inefficient to

            // Set aside header here.

            AbstractNetMsg.allocHeader(writer, this.getHeaderSize());

            // Now serialize the object's specifics

            try {
                this._serialize(writer);

                //Serialize successful, update NetMsgStat

                stat.updateStat(i);

            } catch (BufferOverflowException boe) {
                Logger.error("BufferSize PowerOfTwo: " + i
                        + " is too small for " + protocolMsg != null? protocolMsg.name() : this.getClass().getName() +  ", trying again with " + (i + 1));

                //Return buffer.

                Network.byteBufferPool.putBuffer(bb);
                continue;

            } catch (Exception e) {

                //Return buffer.
            	Logger.error(e);
            	e.printStackTrace();

                Network.byteBufferPool.putBuffer(bb);
                return null;
            }

			// This shouldn't throw any errors since this part of the BB has
            // already been allocated

            this.writeHeaderAt(startPos, writer);
            return writer.getBb();
        }

		// If we get here, its not a successful serialization and lastError
        // should be set

        return null;
    }

    private static void allocHeader(ByteBufferWriter writer, int bytes) {
        byte zero = 0; // prevents the int->byte cast
        for (int h = 0; h < bytes; ++h) {
            writer.put(zero);
        }
    }

    /**
     * Function allows for setting other than default initial Buffer size for
     * the Serializer. Override and return the size of the buffer in power of
     * two bytes.
     *
     * Example, if you would like a buffer of 65535, then return 16 from this
     * value since 2^16 = 65535
     *
     * @return the power to raise two to.
     */
    protected int getPowerOfTwoBufferSize() {
        return (10); // 2^10 == 1024
    }

    /**
     * Forces subclass to define how large (in bytes) the message's header is.
     *
     * @return the length (in bytes) of this message type's header.
     */
    protected abstract int getHeaderSize();

    /**
     * Forces subclasses to implement how to write its own header into a
     * byteBuffer
     *
     * @param startPos
     * - starting position for the header write
     * @param writer
     * - ByteBufferWriter to write the header to.
     */
    protected abstract void writeHeaderAt(int startPos, ByteBufferWriter writer);

    /**
     * @return The protocolMsg of this Msg.
     */
    public Protocol getProtocolMsg() {
        return protocolMsg;
    }

    /**
     * @return The protocolMsg As a string.
     */
    public String getOpcodeAsString() {
        return StringUtils.toHexString(protocolMsg.opcode);
    }

    /**
     * @return the origin
     */
    public AbstractConnection getOrigin() {
        return origin;
    }

    public void setOrigin(AbstractConnection conn) {
        this.origin = conn;
    }

}
