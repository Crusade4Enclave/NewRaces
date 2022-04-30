// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net;

import engine.net.client.Protocol;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NetMsgStat {

    private final Protocol protocolMsg;
    private final AtomicLong total = new AtomicLong();
    private final AtomicLong count = new AtomicLong();
    private final AtomicInteger average = new AtomicInteger();
    private final AtomicInteger max = new AtomicInteger();
    private final AtomicInteger countUnderAverage = new AtomicInteger();
    private final AtomicInteger countOverAverage = new AtomicInteger();
    private final AtomicInteger countOverMax = new AtomicInteger();

    public NetMsgStat(Protocol protocolMsg, int startSize) {
        
        if (startSize < 10)
            startSize = 10;
        
        if (startSize > 30)
            startSize = 30;

        this.protocolMsg = protocolMsg;
        this.total.set(startSize);
        this.count.set(1L);
        this.average.set(10);
        this.max.set(startSize);
        this.countUnderAverage.set(0);
        this.countOverAverage.set(0);
        this.countOverMax.set(0);
    }

    public void updateStat(int i) {
        this.total.addAndGet(i);
        this.count.incrementAndGet();

        int avg = (int) (this.total.get() / this.count.get());
        if (avg < 0)
            avg = 0;
        else if (avg > 30)
            avg = 30;
        else
            this.average.set(avg);

        if (this.max.get() < i)
            this.max.set(i);
        
        if (i <= avg)
            this.countUnderAverage.incrementAndGet();
        else if (i < this.max.get())
            this.countOverAverage.incrementAndGet();
        else
            this.countOverMax.incrementAndGet();
    }

    public Protocol getOpcode() {
        return this.protocolMsg;
    }

    public int getMax() {
        return this.max.get();
    }

}
