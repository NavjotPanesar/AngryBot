import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Hooker {
    private static final int cost = 10;
    private static final String[] diseases = {"Space Aids", "Autism", "Regular Aids", "Chlamydia", "Gonorrhoea", "Syphilis", "Herpes", "Crabs"};
    private static final Random random = new Random();
    static int bananaCost;

    public static void run(MessageReceivedEvent event) {
        bananaCost = cost;
        try {
            DBTools.openConnection();
            ResultSet authorSet = DBTools.selectGUILD_USER(event.getGuild().getId(), event.getAuthor().getId());
            List<Member> mentionedUsers = event.getMessage().getMentions().getMembers();
            if (!mentionedUsers.isEmpty()) bananaCost = cost * mentionedUsers.size();
            assert authorSet != null;
            if (bananaCost > authorSet.getInt("BANANA_CURRENT")) return;
            bananaCost = DBTools.selectGUILD_USER(event.getGuild().getId(), event.getAuthor().getId()).getInt("BANANA_CURRENT") - bananaCost;
            DBTools.updateGUILD_USER(event.getGuild().getId(), event.getAuthor().getId(), null, bananaCost, null, null, null, null, null);
            if (mentionedUsers.isEmpty()) hooker(event, Objects.requireNonNull(event.getMember()));
            else for (Member m : mentionedUsers) hooker(event, m);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void hooker(MessageReceivedEvent event, Member m) {
        String currentName = m.getEffectiveName();
        if (m.getId().equals(Objects.requireNonNull(event.getMember()).getId()))
            event.getChannel().sendMessage(currentName + " has rented themselves a hooker for " + cost + " bananas!").queue();
        else
            event.getChannel().sendMessage(event.getMember().getEffectiveName() + " has rented " + currentName + " a hooker for " + cost + " bananas!").queue();

        try {
            ResultSet authorSet = DBTools.selectGUILD_USER(event.getGuild().getId(), m.getUser().getId());
            assert authorSet != null;
            int hookerCount = 1 + authorSet.getInt("HOOKER");
            String std = authorSet.getString("STD");
            if (random.nextInt(100) < 30) {
                String disease = diseases[random.nextInt(diseases.length - 1)];
                std = stdList(disease, authorSet.getString("STD"));
                event.getGuild().modifyNickname(m, buildName(m, disease)).queue();
                event.getChannel().sendMessage("Uh oh! " + currentName + " caught " + disease + ".").queue();
            }
            DBTools.updateGUILD_USER(event.getGuild().getId(), m.getUser().getId(), null, bananaCost, null, null, null, hookerCount, std);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String buildName(Member m, String disease) {
        String nickname;
        if (m.getNickname().equals("null")) nickname = m.getUser().getName();
        else nickname = m.getNickname();
        String newName = "*" + disease + "* " + nickname;
        if (newName.length() > 32) newName = newName.substring(0, 32);

        return newName;
    }

    public static String stdList(String disease, String std) {
        if (std.isEmpty() || std.isBlank()) std = disease;
        else if (!std.contains(disease)) std += ", " + disease;
        return std;
    }

}


