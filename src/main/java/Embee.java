import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class Embee {

    public static void run(MessageReceivedEvent event) {
        ListIterator<Role> roles = Objects.requireNonNull(event.getMember()).getRoles().listIterator();
        while (roles.hasNext()) {
            if (roles.next().hasPermission(Permission.MODERATE_MEMBERS) || event.getAuthor().getId().equals("328689134606614528")) {

                System.out.println("Embee Run   ");
                Message msg = event.getMessage();
                User author = msg.getAuthor();              // author object
                String ID = author.getId();                 //unique user ID
                String nickname = event.getMessage().getContentRaw().substring(7);
                System.out.println("Embee Run   " + ID);
                if (ID.equals("328689134606614528")) {
                    System.out.println("Gorb Messaged  " + nickname);
                    event.getMessage().delete();
                    event.getGuild().modifyNickname(Objects.requireNonNull(event.getGuild().getMemberById("289219467265966080")), nickname).queue();

                    System.out.println("Nickname Set");

                    System.out.println("Message Delete");
                }
                return;
            }
        }

    }}