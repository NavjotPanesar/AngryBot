
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


import java.sql.SQLException;
import java.util.*;

public class UnGunk {

    public static void run(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        User author = msg.getAuthor();              // author object
        String ID = author.getId();                 //unique user ID
        int bananaCost = 0;
        int gunked = 1;
        List<User> users = msg.getMentions().getUsers();  //list of tagged users
        int userCount = users.toArray().length;
        try {
            DBTools.openConnection();
            System.out.println(event.getGuild().getId()+ "  , "+ ID);
            if (userCount * 5 > DBTools.selectGUILD_USER(event.getGuild().getId(), ID).getInt("BANANA_CURRENT")) {
                return;
            } else for (User u : users) {

                ID = u.getId();
                String nickname = event.getGuild().getMemberById(ID).getUser().getName();
                System.out.println(nickname);
                event.getGuild().modifyNickname(Objects.requireNonNull(event.getGuild().getMemberById(ID)), nickname).queue();
                bananaCost += 5;


            }


            bananaCost = DBTools.selectGUILD_USER(event.getGuild().getId(), author.getId()).getInt("BANANA_CURRENT") - bananaCost;


            DBTools.updateGUILD_USER(event.getGuild().getId(), author.getId(), null, bananaCost, null, null);

            DBTools.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
