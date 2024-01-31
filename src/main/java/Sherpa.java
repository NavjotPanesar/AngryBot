import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.security.auth.login.LoginException;
import java.nio.channels.Channel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sherpa {
    static List TriggerWords = new ArrayList<>();

    public static void run(MessageReceivedEvent mostRecentEvent) {
        String commandContent = "";
        String argument = "";
        String content = mostRecentEvent.getMessage().getContentRaw().toLowerCase();
        if (content.contains(" ")) {
            commandContent = content.split(" ")[1].toLowerCase().trim();
            if (content.substring(content.indexOf(content.split(" ")[1].toLowerCase().trim())).contains(" ")) {
                argument = content.substring(content.indexOf(content.split(" ")[2].toLowerCase().trim()));
            }
        }
        System.out.println("commandContent: " + commandContent);
        System.out.println("argument" + argument);
        switch (commandContent) {
            case "list":
                list(mostRecentEvent);
                break;
            case "add":
                try {
                    addTrigger(argument);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "remove":
            case "rm":
                try {
                    removeTrigger(argument);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "h":
            case "help":
            case "?":
            case "man":
                help(mostRecentEvent);
                break;

            default:
                replySherpa(mostRecentEvent);
                break;
        }
    }

    public static void initializeList() throws SQLException {
        DBTools.openConnection();
        ResultSet result = DBTools.getCOMMAND_KEYWORD("sherpalist");
        DBTools.closeConnection();
        assert result != null;
        result.next();
        TriggerWords = Tools.csvToList(result.getString("KEYWORD"));
        System.out.println("Sherpa list initialized: " + result.getString("KEYWORD"));

    }

    public static void replySherpa(MessageReceivedEvent event) {
        System.out.println("sherpa reply method run");
        event.getMessage().reply("Hey you " + Tools.generateSillyWord()+", try this next time https://angrysnowboarder.bigcartel.com/product/your-personal-snowboard-gear-shopper-sherpa").queue();
    }


    public static void removeTrigger(String input) throws SQLException {
        System.out.println("sherpa remove trigger method run");
        if (TriggerWords.contains(input)) {
            TriggerWords.remove(input.toLowerCase());
            String update = Tools.listToCsv(TriggerWords);
            DBTools.openConnection();
            DBTools.updateCOMMAND_KEYWORD("sherpalist", update);
            DBTools.closeConnection();
        }
    }

    public static void addTrigger(String input) throws SQLException {
        System.out.println("sherpa add trigger method run");
        if (!input.contains(";") && !TriggerWords.contains(input.toLowerCase())) {
            TriggerWords.add(input.toLowerCase());
            String update = Tools.listToCsv(TriggerWords);
            System.out.println(update);
            DBTools.openConnection();
            DBTools.updateCOMMAND_KEYWORD("sherpalist", update);
            DBTools.closeConnection();
        }
    }


    public static void help(MessageReceivedEvent event) {
        System.out.println("sherpa help method run");
      AngryBot.eb.clear();
        AngryBot.eb.setTitle("Shopping Sherpa Commands");
        AngryBot.eb.addField("sherpa","replies with the shopping sherpa link",false);
        AngryBot.eb.addField("sherpa list","Lists the current trigger words",false);
        AngryBot.eb.addField("sherpa add <arg>","adds a new trigger word",false);
        AngryBot.eb.addField("sherpa remove <arg>","removes a trigger word",false);

        event.getGuildChannel().sendMessageEmbeds(AngryBot.eb.build()).queue();

    }

    private static void list(MessageReceivedEvent event) {
        System.out.println("sherpa list method run");
        AngryBot.eb.clear();
        AngryBot.eb.setTitle("Shopping Sherpa Trigger Words: ");
        AngryBot.eb.addField(new MessageEmbed.Field(TriggerWords.toString(),"",false));

        event.getGuildChannel().sendMessageEmbeds(AngryBot.eb.build()).queue();
    }
}
