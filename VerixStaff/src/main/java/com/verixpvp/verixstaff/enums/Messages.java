package com.verixpvp.verixstaff.enums;

import com.gameservergroup.gsgcore.utils.Text;

public enum Messages {

    PLAYER_OFFLINE("&c&lERROR &8» &c{player} is offline!"),

    SQL_ERROR("&c&lERROR &8» &cThere was an SQL error doing this command. Please report this to a VerixPVP Developer."),

    COMMAND_REPORT_SUCCESS("&a&lSUCCESS &8» &aYou have successfully sent in a report on &f{player} &afor &f{reason}"),
    COMMAND_REPORT_REASON_MUST_BE_LONGER("&c&lERROR &8» &cYour report reason must be longer than 10 characters!"),
    COMMAND_REPORT_CANT_REPORT_YOURSELF("&c&lERROR &8» &cYou can't report yourself!"),

    COMMAND_RANDOMTP_MUST_BE_IN_STAFFMODE("&c&lERROR &8» &cYou must be in staffmode to random teleport!"),
    COMMAND_RANDOMTP_SENT("&a&lSUCCESS &8» &aYou have been randomly sent to &f{player}");

    private String rawMessage;

    Messages(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public String getKey() {
        return name().toLowerCase().replace('_', '-');
    }

    public String getMessage() {
        return Text.toColor(rawMessage);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
