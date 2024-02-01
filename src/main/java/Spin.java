import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class Spin {

    public static void run(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        User author = msg.getAuthor();              // author object
        String ID = author.getId();                 // unique user ID
        System.out.println("ID: " + ID);
        int bananaCost = 5;
        int userBalance;
        int totalBananas;
        Random random = new Random();
        int dropChance = random.nextInt(3); // 0 or 1
        int winnings = 0;
        try {
            DBTools.openConnection();
            ResultSet authorSet = DBTools.selectGUILD_USER(event.getGuild().getId(), ID);
            userBalance = authorSet.getInt("BANANA_CURRENT");
            totalBananas = authorSet.getInt("BANANA_TOTAL");
            if (userBalance < bananaCost) {
                event.getChannel().sendMessage(author.getAsMention() + " You don't have enough bananas to spin!").queue();
                return;
            } else {

            userBalance -= bananaCost;
            System.out.println("DropChance: " + dropChance);
            if (dropChance != 0) {
                // 50% chance to drop 5 bananas
                final String[] MEANMESSAGE = {"You are so stupid!", "Thats what you get for gambling, idiot.", "Get gunked!", "Hahahahaha", "You should give up", "Stop gambling!"};
                userBalance -= 5;
                int arrayLength = MEANMESSAGE.length;
                Random r = new Random();
                int randomIndex = r.nextInt(arrayLength);
                String randomMeanMessage = MEANMESSAGE[randomIndex];

                event.getChannel().sendMessage(author.getAsMention() + " Uh-oh! You dropped 5 bananas on the way to the slot machine! 🍌🍌🍌🍌🍌 " + randomMeanMessage).queue();
                    int currentJackpot = DBTools.selectJACKPOT();
                    System.out.println("Current Jackpot: " + currentJackpot);
                    currentJackpot += 5;
                    DBTools.updateJACKPOT(currentJackpot);
                    event.getChannel().sendMessage(":rotating_light:Banana Jackpot has reached: " + currentJackpot + " bananas! :rotating_light:").queue();
            } else {
                // 50% chance to spin
                // Determine the spin result
                
                int spinResult = random.nextInt(100) + 1;
                System.out.println("Spinning for nanas....");
                // Adjust the chances and winnings based on your preferences
                if (spinResult <= 30) {
                    // 30% chance to win 5 bananas
                    winnings = 5;
                    event.getChannel().sendMessage(author.getAsMention() + " You spun and won 5 bananas! (You're an idiot!)").queue();
                } else if (spinResult <= 60) {
                    // 30% chance to win 10 bananas
                    winnings=10;
                    event.getChannel().sendMessage(author.getAsMention() + " You spun and won 10 bananas! Good job I guess?").queue();
                } else if (spinResult <= 80) {
                    // 20% chance to win 15 bananas
                    winnings=15;
                    event.getChannel().sendMessage(author.getAsMention() + " You spun and won 15 bananas! Thats cool").queue();
                } else if (spinResult <= 95) {
                    // 15% chance to win 20 bananas
                    winnings=20;
                    event.getChannel().sendMessage(author.getAsMention() + " You spun and won 20 bananas! Nice.").queue();
                } else {
                    // 5% chance to win the jackpot (5 bananas)
                        int jackpot = DBTools.selectJACKPOT();
                        winnings = jackpot;
                        event.getChannel().sendMessage(author.getAsMention() + " 🎉🎉🎉 Jackpot! You spun and won " + jackpot + " bananas! 🎉🎉🎉 Holy moly!").queue();    
                        DBTools.updateJACKPOT(25);
                        event.getChannel().sendMessage("The banana Jackpot has reset to 25 bananas! Good luck!").queue();
                }
            }

            userBalance = userBalance + winnings;
            totalBananas = totalBananas + winnings;

            // Update the user's balance and the jackpot in the database
            
            DBTools.updateGUILD_USER(event.getGuild().getId(), ID, totalBananas, userBalance, null, null);
            
            DBTools.closeConnection();
        }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
