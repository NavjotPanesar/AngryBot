import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class Spin extends ListenerAdapter {

    private static final int BANANA_COST = 5;
    private static final Random random = new Random();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().getId().equals("lleters")) {
            handleLletersMessage(event);
        } else {
            processSpin(event);
        }
    }

    private void handleLletersMessage(MessageReceivedEvent event) {
        try {
            DBTools.openConnection();
            int userBalance = DBTools.selectGUILD_USER(event.getGuild().getId(), event.getAuthor().getId()).getInt("BANANA_CURRENT");
            if (userBalance >= 1) {
                DBTools.updateGUILD_USER(event.getGuild().getId(), event.getAuthor().getId(), null, userBalance - 1, null, null);
            } else {
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + " lmao get more bananas").queue();
            }
            DBTools.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void processSpin(MessageReceivedEvent event) {
        User author = event.getAuthor();
        String userId = author.getId();
        String guildId = event.getGuild().getId();

        try {
            DBTools.openConnection();
            ResultSet authorSet = DBTools.selectGUILD_USER(guildId, userId);
            int userBalance = authorSet.getInt("BANANA_CURRENT");

            if (userBalance < BANANA_COST) {
                event.getChannel().sendMessage(author.getAsMention() + " You don't have enough bananas to spin!").queue();
                return;
            }

            int totalBananas = authorSet.getInt("BANANA_TOTAL");
            userBalance -= BANANA_COST;

            int winnings = random.nextInt(3) == 0 ? 0 : calculateWinnings(event, author);

            userBalance += winnings;
            totalBananas += winnings;

            DBTools.updateGUILD_USER(guildId, userId, totalBananas, userBalance, null, null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBTools.closeConnection();
        }
    }

    private int calculateWinnings(MessageReceivedEvent event, User author) {
        int spinResult = random.nextInt(100) + 1;
        int winnings = switch (spinResult) {
            case 1 -> 5;
            case 2, 3, 4, 5 -> 10;
            case 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 -> 15;
            case 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 -> 20;
            default -> handleJackpot(event, author);
        };
        announceWinnings(event, author, winnings);
        return winnings;
    }

    private int handleJackpot(MessageReceivedEvent event, User author) {
        int jackpot = DBTools.selectJACKPOT();
        event.getChannel().sendMessage(author.getAsMention() + " 🎉🎉🎉 Jackpot! You spun and won " + jackpot + " bananas! 🎉🎉🎉 Holy moly!").queue();
        DBTools.updateJACKPOT(25);
        event.getChannel().sendMessage("The banana Jackpot has reset to 25 bananas! Good luck!").queue();
        return jackpot;
    }

    private void announceWinnings(MessageReceivedEvent event, User author, int winnings) {
        String winningMessage = switch (winnings) {
            case 5 -> " You spun and won 5 bananas! (You're an idiot!)";
            case 10 -> " You spun and won 10 bananas! Good job I guess?";
            case 15 -> " You spun and won 15 bananas! Thats cool";
            case 20 -> " You spun and won 20 bananas! Nice.";
            default -> " You spun and won " + winnings + " bananas!";
        };
        event.getChannel().sendMessage(author.getAsMention() + winningMessage).queue();
    }
}