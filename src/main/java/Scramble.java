import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class Scramble {

    public static void run(MessageReceivedEvent event) {
        ListIterator<Role> roles = Objects.requireNonNull(event.getMember()).getRoles().listIterator();
        while (roles.hasNext()) {
            if (roles.next().hasPermission(Permission.MODERATE_MEMBERS) || event.getAuthor().getId().equals("328689134606614528")) {

                Message msg = event.getMessage();
                User author = msg.getAuthor();              // author object
                String ID = author.getId();                 //unique user ID
                String nickname = Objects.requireNonNull(event.getMember()).getEffectiveName();

                List<User> users = msg.getMentions().getUsers();  //list of tagged users

                for (User u : users) {
                    ID = u.getId();
                    if (ID != "617171699325993000") {
                        nickname = Tools.stringScramble(Objects.requireNonNull(event.getGuild().getMemberById(u.getId())).getEffectiveName());
                        nickname = nickname.substring(0, 1).toUpperCase() + nickname.substring(1);
                        event.getGuild().modifyNickname(Objects.requireNonNull(event.getGuild().getMemberById(ID)), nickname).queue();
                    }
                }
                return;
            }
        }
    }
}
