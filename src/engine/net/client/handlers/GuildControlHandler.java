// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.handlers;

import engine.Enum;
import engine.exception.MsgSendException;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.guild.GuildControlMsg;
import engine.net.client.msg.guild.GuildListMsg;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;

public class GuildControlHandler extends AbstractClientMsgHandler {

	public GuildControlHandler() {
		super(GuildControlMsg.class);
	}
	
	// TODO Don't think this protocolMsg (0x3235E5EA) is actually player history. so
	// take further look at it.
	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {
		GuildControlMsg msg = (GuildControlMsg) baseMsg;
        Dispatch dispatch;

		// until we know what it's for, just echo it back.
		msg.setUnknown05((byte) 1);

		//Send a GuildList msg
		if(msg.getUnknown01() == 1) {

			PlayerCharacter player = SessionManager.getPlayerCharacter(origin);
			//TODO figure out why GL can't be changed and IC can't be banished
			//Bounce back the rank options
			msg.setGM((byte) (GuildStatusController.isGuildLeader(player.getGuildStatus()) ? 1 : 0));

            dispatch = Dispatch.borrow(player, msg);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

            if (GuildStatusController.isInnerCouncil(player.getGuildStatus()) || GuildStatusController.isGuildLeader(player.getGuildStatus())) {
                dispatch = Dispatch.borrow(player, new GuildListMsg(player.getGuild()));
                DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
            }   else
                ErrorPopupMsg.sendErrorMsg(player, "Only guild leader and inner council have such authority!");


        } else if(msg.getUnknown01() == 2) {
			PlayerCharacter player = SessionManager.getPlayerCharacter(origin);
			
			//If we don't get a valid PC for whatever reason.. just ignore it.
			PlayerCharacter pc = PlayerCharacter.getFromCache(msg.getUnknown03());

			if(pc != null) {
				dispatch = Dispatch.borrow(player,new GuildListMsg(pc));
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			}
		}
		
		return true;
	}

}
