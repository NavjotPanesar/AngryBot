
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Gunk {

    public static void run(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        User author = msg.getAuthor();              // author object
        String ID = author.getId();                 //unique user ID


        int bananaCost = 0;
        int gunked = 1;
        int gunks = 1;
        String newname = "";
        List<User> users = msg.getMentions().getUsers();  //list of tagged users
        int userCount = users.toArray().length;
        try {
            DBTools.openConnection();
            ResultSet authorSet = DBTools.selectGUILD_USER(event.getGuild().getId(), ID);
            gunks = authorSet.getInt("GUNKS");
                    System.out.println(event.getGuild().getId()+ "  , "+ ID);
            if (userCount * 5 > authorSet.getInt("BANANA_CURRENT")) {
                return;
            } else for (User u : users) {
                try {
                    ID = u.getId();
                    newname = Tools.generateSillyWord();

                    event.getGuild().modifyNickname(Objects.requireNonNull(event.getGuild().getMemberById(ID)), newname).queue();
                    event.getChannel().sendMessage(u.getEffectiveName() + " just got GUNKED! Their new name is " + newname).queue();
                    bananaCost += 5;
                    gunks += 1;
                    gunked = 1 + DBTools.selectGUILD_USER(event.getGuild().getId(), ID).getInt("GUNKED");
                    System.out.println("gunked " + gunked);

                    DBTools.updateGUILD_USER(event.getGuild().getId(), ID, null, null, gunked, null);
                }catch(HierarchyException e){

                }

            }


            bananaCost = DBTools.selectGUILD_USER(event.getGuild().getId(), author.getId()).getInt("BANANA_CURRENT") - bananaCost;


            DBTools.updateGUILD_USER(event.getGuild().getId(), author.getId(), null, bananaCost, null, gunks);

            DBTools.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
