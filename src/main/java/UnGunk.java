
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UnGunk {

    public static void run(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        User author = msg.getAuthor();              // author object
        String ID = author.getId();                 //unique user ID
        int bananaCost = 0;
        List<User> users = msg.getMentions().getUsers();  //list of tagged users
        int userCount = users.toArray().length;
        try {
            DBTools.openConnection();
            ResultSet authorSet = DBTools.selectGUILD_USER(event.getGuild().getId(), ID);

            System.out.println(event.getGuild().getId()+ "  , "+ ID);
            if (userCount * 20 > authorSet.getInt("BANANA_CURRENT")) {
                return;
            } else for (User u : users) {

                ID = u.getId();
                event.getGuild().modifyNickname(Objects.requireNonNull(event.getGuild().getMemberById(ID)), u.getGlobalName()).queue();
                bananaCost += 20;


            }


            bananaCost = DBTools.selectGUILD_USER(event.getGuild().getId(), author.getId()).getInt("BANANA_CURRENT") - bananaCost;


            DBTools.updateGUILD_USER(event.getGuild().getId(), author.getId(), null, bananaCost, null, null,null);

            DBTools.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
