package kts.dev.ktsbk.ktsbkbot.commands;

import kts.dev.Main;
import kts.dev.ktsbk.common.db.users.KtsUser;
import kts.dev.ktsbk.common.utils.AuthContext;
import kts.dev.ktsbk.common.utils.KbErr;
import kts.dev.ktsbk.common.utils.Pair;
import kts.dev.ktsbk.server.KtsDbSource;
import kts.dev.ktsbk.server.csv.VirtualCsvWriter;
import kts.dev.ktsbk.server.dao.users.KtsUserDao;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

public class AdministrationCommands extends ListenerAdapter {
    private final KtsUserDao daoUser = KtsDbSource.INSTANCE.createDao(KtsUser.class);
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("create_user")) {
            //
            try {
                List<KtsUser> users = daoUser.queryForEq("disid", event.getUser().getId());
                if(users.isEmpty()) {
                    event.reply("Your disid not in db.").setEphemeral(true).queue();
                    return;
                }
                if(event.getOption("user_ping", OptionMapping::getAsMember) == null) {
                    event.reply("Member not found.").setEphemeral(true).queue();
                    return;
                }
                KbErr e = Main.service.createUser(
                        new AuthContext(users.get(0)),
                        event.getOption("nickname", OptionMapping::getAsString),
                        event.getOption("user_ping", OptionMapping::getAsMember).getUser().getId()
                );
                event.reply(e.translatable()).setEphemeral(true).queue();
            } catch (SQLException e) {
                event.reply("Application error.").setEphemeral(true).queue();
            }
        } else if(event.getName().equals("give_warn")) {
            try {
                List<KtsUser> users = daoUser.queryForEq("disid", event.getUser().getId());
                if (users.isEmpty()) {
                    event.reply("Your disid not in db.").setEphemeral(true).queue();
                    return;
                }
                KbErr e = Main.service.giveWarn(
                        new AuthContext(users.get(0)),
                        (long)event.getOption("user_id", OptionMapping::getAsInt),
                        (long)event.getOption("day_period", OptionMapping::getAsInt),
                        (long)event.getOption("punishment_points", OptionMapping::getAsInt),
                        event.getOption("ban", OptionMapping::getAsBoolean),
                        (long)event.getOption("fine", OptionMapping::getAsInt),
                        event.getOption("message", OptionMapping::getAsString)
                );
                event.reply(e.translatable()).setEphemeral(true).queue();
            } catch (SQLException e) {
                event.reply("Application error.").setEphemeral(true).queue();
            }
        } else if(event.getName().equals("token_reset")) {
            try {
                List<KtsUser> users = daoUser.queryForEq("disid", event.getUser().getId());
                if (users.isEmpty()) {
                    event.reply("Your disid not in db.").setEphemeral(true).queue();
                    return;
                }

                KbErr e = Main.service.resetPassword(
                        new AuthContext(users.get(0)),
                        (long)event.getOption("user_id", OptionMapping::getAsInt)
                );
                event.reply(e.translatable()).setEphemeral(true).queue();
            } catch (SQLException e) {
                event.reply("Application error.").setEphemeral(true).queue();
            }
        } else if(event.getName().equals("testing")) {
            /*
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputStreamWriter w = new OutputStreamWriter(outputStream);


            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader("testing 1", "2", "3")
                    .build();

            try (final CSVPrinter printer = new CSVPrinter(w, csvFormat)) {
                printer.printRecord(1, 2, 3);
                printer.printRecord(event, "", "%7");
            } catch (IOException ignore) {}
            InputStream s = new ByteArrayInputStream(outputStream.toByteArray());
            event.replyFiles(FileUpload.fromData(s, "testing.csv")).queue();

             */

            try(VirtualCsvWriter w = new VirtualCsvWriter("testing.csv", "1", "2", "!@", ",")) {
                w.printer.printRecord(1, 2, 3);
                w.printer.printRecord(event, "", "%7");
                event.replyFiles(w.getFileUpload()).queue();
            } catch (IOException exception) {
                event.reply(exception.getMessage()).queue();
            }

        }
    }
}
