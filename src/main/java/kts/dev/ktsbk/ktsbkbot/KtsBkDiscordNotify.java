package kts.dev.ktsbk.ktsbkbot;

import kts.dev.ktsbk.common.db.accounts.KtsAccount;
import kts.dev.ktsbk.common.db.box.KtsBox;
import kts.dev.ktsbk.common.db.bridge.KtsUserAccount;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.ktsbkbot.commands.AdministrationCommands;
import kts.dev.ktsbk.server.KtsDbSource;
import kts.dev.ktsbk.server.dao.accounts.KtsAccountDao;
import kts.dev.ktsbk.server.dao.bridge.KtsUserAccountDao;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.sql.SQLException;
import java.util.List;

public class KtsBkDiscordNotify {
    private final JDA jda;
    KtsAccountDao accDao;
    KtsUserAccountDao accUsrDao;
    public KtsBkDiscordNotify() {
         jda = JDABuilder
                .createDefault("TOKEN")
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new AdministrationCommands())
                .build();

         jda.updateCommands().addCommands(
                 Commands.slash("create_user", "create user in ktsbk.")
                         .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                         .addOption(OptionType.STRING, "nickname", "nickname", true)
                         .addOption(OptionType.MENTIONABLE, "user_ping", "mention", true),
                 Commands.slash("give_warn", "give warn to user.")
                         .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                         .addOption(OptionType.INTEGER, "user_id", "user id", true)
                         .addOption(OptionType.INTEGER, "day_period", "day period", true)
                         .addOption(OptionType.INTEGER, "punishment_points", "points", true)
                         .addOption(OptionType.BOOLEAN, "ban", "ban", true)
                         .addOption(OptionType.INTEGER, "fine", "fine", true)
                         .addOption(OptionType.STRING, "message", "about", true),
                 Commands.slash("token_reset", "token reset")
                         .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                         .addOption(OptionType.INTEGER, "user_id", "user id", true),
                 Commands.slash("testing", "testing")
                         .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
         ).queue();

        accDao = KtsDbSource.INSTANCE.createDao(KtsAccount.class);
        accUsrDao = KtsDbSource.INSTANCE.createDao(KtsUserAccount.class);

    }
    public void send(String disid, String message) {
        User u = jda.getUserById(disid);
        if(u == null) {
            try {
                u = jda.retrieveUserById(disid).complete();
            } catch (Exception e) {
                System.out.println("User not found.");
                return;
            }
        }

        if(message.length() >= 2048) {
            for(int i = 0; i < message.length() / 2048; i++) {
                String sub_message = message.substring(i * 2048, (i+1) * 2048);
                u.openPrivateChannel().flatMap(cnl -> cnl.sendMessage(sub_message)).queue();
            }
        } else {
            u.openPrivateChannel().flatMap(cnl -> cnl.sendMessage(message)).queue();
        }
    }

    public void sendFiles(String disid, FileUpload... files) {
        User u = jda.getUserById(disid);
        if(u == null) {
            try {
                u = jda.retrieveUserById(disid).complete();
            } catch (Exception e) {
                System.out.println("User not found.");
                return;
            }
        }
        u.openPrivateChannel().flatMap(cnl -> cnl.sendFiles(files)).queue();
    }

    public void sendFiles(KtsUser user, FileUpload... files) {
        sendFiles(user.getDisId(), files);
    }

    public void send(KtsUser user, String message) {
        send(user.getDisId(), message);
    }

    public void send(List<KtsUser> users, String message) {
        for(KtsUser u : users) {
            send(u, message);
        }
    }

    public void send(KtsAccount acc, String message) throws SQLException {
        send(acc.getUser(), message);
        send(accUsrDao.getMemberShip(acc), message);
    }

    public void send(KtsBox box, String message) throws SQLException {
        send(box.getAccount(), message);
    }


    public void threadSend(KtsUser u, String message) {
        new SendThread(List.of(u), message).start();
    }

    public void threadSend(List<KtsUser> u, String message) {
        new SendThread(u, message).start();
    }

    class SendThread extends Thread {
        private final List<KtsUser> ids;
        private final String message;
        public SendThread(List<KtsUser> ids, String message) {
            this.ids = ids;
            this.message = message;
        }
        @Override
        public void run() {
            send(ids, message);
        }
    }
}
