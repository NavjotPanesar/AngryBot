import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class Spin {
    private static final int BANANA_COST = 5;
    private static final Random random = new Random();

    public static void run(MessageReceivedEvent event) {
        User author = event.getAuthor();
        String userId = author.getId();
        String guildId = event.getGuild().getId();
        int userBalance = 0;
        int totalBananas = 0;

        try {
            ResultSet authorSet = DBTools.selectUserBalance(guildId, userId);
            if (authorSet.next()) {
                userBalance = authorSet.getInt("BANANA_CURRENT");
                totalBananas = authorSet.getInt("BANANA_TOTAL");
            }

            if (!hasEnoughBananas(userBalance)) {
                sendMessageNotEnoughBananas(event, author);
                return;
            }

            userBalance -= BANANA_COST;
            int[] newBalances = processSpin(event, author, userBalance, totalBananas);

            if (userId.equals("lleters")) {
                transferBananas(event, "tragon_", "jbasilious", 2);
            }

            DBTools.updateUserBalances(guildId, userId, newBalances[0], newBalances[1]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean hasEnoughBananas(int userBalance) {
        return userBalance >= BANANA_COST;
    }

    private static void sendMessageNotEnoughBananas(MessageReceivedEvent event, User author) {
        event.getChannel().sendMessage(author.getAsMention() + " You don't have enough bananas to spin!").queue();
    }

    private static int[] processSpin(MessageReceivedEvent event, User author, int userBalance, int totalBananas) {
        int dropChance = random.nextInt(3); // 0, 1, or 2

        if (dropChance != 0) {
            handleDropChance(event, author);
        } else {
            int winnings = calculateWinnings(event, author);
            userBalance += winnings;
            totalBananas += winnings;
        }

        return new int[]{userBalance, totalBananas};
    }

    private static void handleDropChance(MessageReceivedEvent event, User author) {
        final String[] MEAN_MESSAGES = {
                "You are so stupid!", "Thats what you get for gambling, idiot.",
                "Get gunked!", "Hahahahaha", "You should give up", "Stop gambling!"
        };

        String randomMeanMessage = MEAN_MESSAGES[random.nextInt(MEAN_MESSAGES.length)];
        event.getChannel().sendMessage(author.getAsMention() + " Uh-oh! You dropped 5 bananas on the way to the slot machine! 🍌🍌🍌🍌🍌 " + randomMeanMessage).queue();

        int currentJackpot = DBTools.selectJackpot();
        currentJackpot += 5;
        DBTools.updateJackpot(currentJackpot);
        event.getChannel().sendMessage(":rotating_light:Banana Jackpot has reached: " + currentJackpot + " bananas! :rotating_light:").queue();
    }

    private static int calculateWinnings(MessageReceivedEvent event, User author) {
        int spinResult = random.nextInt(100) + 1;
        int winnings = 0;

        if (spinResult <= 30) {
            winnings = 5;
        } else if (spinResult <= 60) {
            winnings = 10;
        } else if (spinResult <= 80) {
            winnings = 15;
        } else if (spinResult <= 95) {
            winnings = 20;
        } else {
            winnings = handleJackpot(event, author);
        }

        announceWinnings(event, author, winnings);
        return winnings;
    }

    private static int handleJackpot(MessageReceivedEvent event, User author) {
        int jackpot = DBTools.selectJackpot();
        event.getChannel().sendMessage(author.getAsMention() + " 🎉🎉🎉 Jackpot! You spun and won " + jackpot + " bananas! 🎉🎉🎉 Holy moly!").queue();
        DBTools.updateJackpot(25);
        event.getChannel().sendMessage("The banana Jackpot has reset to 25 bananas! Good luck!").queue();
        return jackpot;
    }

    private static void announceWinnings(MessageReceivedEvent event, User author, int winnings) {
        String winningMessage = switch (winnings) {
            case 5 -> " You spun and won 5 bananas! (You're an idiot!)";
            case 10 -> " You spun and won 10 bananas! Good job I guess?";
            case 15 -> " You spun and won 15 bananas! Thats cool";
            case 20 -> " You spun and won 20 bananas! Nice.";
            default -> " You spun and won " + winnings + " bananas!";
        };
        event.getChannel().sendMessage(author.getAsMention() + winningMessage).queue();
    }

    private static void transferBananas(MessageReceivedEvent event, String lleters, String tragon_, String jbasilious, int amount) {
        DBTools.transferBananas(lleters, tragon_, amount);
        DBTools.transferBananas(lleters, jbasilious, amount);
    }
}