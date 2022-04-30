// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.session;

import engine.util.ByteUtils;

public class SessionID {

	private final byte[] data;
	private final String dataAsString;

	public SessionID(byte[] data) {
		super();
		this.data = data;
		this.dataAsString = ByteUtils.byteArrayToStringHex(data);
	}

	@Override
	public boolean equals(Object obj) {
		boolean out = false;
		if(obj instanceof SessionID) {
			out = true;
			SessionID id = (SessionID) obj;
			for (int i = 0; out && i < id.data.length; ++i) {
				if (id.data[i] != this.data[i]) {
					out = false;
				}
			}
		}
		
		return out;
	}

	@Override
	public String toString() {
		return this.dataAsString;
	}

	public final byte[] getData() {
		return data;
	}

}
