// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.objects.SkillReq;

import java.util.ArrayList;

public class dbSkillReqHandler extends dbHandlerBase {

    public dbSkillReqHandler() {
        this.localClass = SkillReq.class;
        this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
    }

    public ArrayList<SkillReq> GET_REQS_FOR_RUNE(final int objectUUID) {
        prepareCallable("SELECT * FROM `static_skill_skillreq` WHERE `runeID`=?");
        setInt(1, objectUUID);
        return getObjectList();
    }
    
    public SkillReq GET_REQS_BY_SKILLID(int skillID) {
        prepareCallable("SELECT * FROM `static_skill_skillreq` WHERE `skillID` = ?");
        setInt(1,skillID);
        int objectUUID = (int) getUUID();
        return (SkillReq) this.getObjectSingle(objectUUID);
    }
}
