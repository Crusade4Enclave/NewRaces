// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.job.AbstractScheduleJob;
import engine.objects.Bane;
import org.joda.time.DateTime;

public class BaneDefaultTimeJob extends AbstractScheduleJob {

    private final Bane bane;

    public BaneDefaultTimeJob(Bane bane) {
        super();
        this.bane = bane;

    }

    @Override
    protected void doJob() {

        //bane already set.
        if (this.bane.getLiveDate() != null) {
            return;
        }

        DateTime defaultTime = new DateTime(this.bane.getPlacementDate());
        defaultTime = defaultTime.plusDays(2);
        defaultTime = defaultTime.hourOfDay().setCopy(22);
        defaultTime = defaultTime.minuteOfHour().setCopy(0);
        defaultTime = defaultTime.secondOfMinute().setCopy(0);
        this.bane.setLiveDate(defaultTime);

    }

    @Override
    protected void _cancelJob() {
    }

}
