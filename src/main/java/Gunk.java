import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Gunk {
    private static int cost = 20;
    private static int userCount;

    public static void gunk(MessageReceivedEvent event, boolean gunk) {
        Message msg = event.getMessage();
        User author = msg.getAuthor();              // author object
        String ID = author.getId();                 //unique user ID
        int bananaCost = 0;
        int gunked;
        int gunks;
        String newname;
        List<User> users = new ArrayList<User>();
        if (event.getMessage().getMentions().mentionsEveryone()) {
            List<Member> members = event.getGuild().getMembers();
            for (Member m : members) {
                users.add(m.getUser());
            }
        }else users = msg.getMentions().getUsers();  //list of tagged users

        int userCount = users.toArray().length;
        try {
            DBTools.openConnection();
            ResultSet authorSet = DBTools.selectGUILD_USER(event.getGuild().getId(), ID);
            gunks = authorSet.getInt("GUNKS");
            System.out.println(event.getGuild().getId() + "  , " + ID);
            if (userCount * cost > authorSet.getInt("BANANA_CURRENT")) {
                event.getChannel().sendMessage(author.getEffectiveName() + " is an idiot and doesn't have enough bananas! ").queue();
                return;
            } else for (User u : users) {
                try {
                    bananaCost += cost;
                    if (gunk) {
                        ID = u.getId();
                        newname = Tools.generateSillyWord();
                        event.getGuild().modifyNickname(Objects.requireNonNull(event.getGuild().getMemberById(ID)), newname).queue();
                        event.getChannel().sendMessage(u.getEffectiveName() + " just got GUNKED! Their new name is " + newname).queue();

                        gunks += 1;
                        gunked = 1 + DBTools.selectGUILD_USER(event.getGuild().getId(), ID).getInt("GUNKED");
                        DBTools.updateGUILD_USER(event.getGuild().getId(), ID, null, null, gunked, null, null);

                    } else {
                        event.getGuild().modifyNickname(Objects.requireNonNull(event.getGuild().getMemberById(ID)), u.getGlobalName()).queue();
                    }
                } catch (HierarchyException e) {

                }

            }


            bananaCost = DBTools.selectGUILD_USER(event.getGuild().getId(), author.getId()).getInt("BANANA_CURRENT") - bananaCost;
            DBTools.updateGUILD_USER(event.getGuild().getId(), author.getId(), null, bananaCost, null, gunks, null);

            DBTools.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


}