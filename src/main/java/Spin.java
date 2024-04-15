import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Spin {

    private static final int BANANA_COST = 5;
    private static final Random random = new Random();
    private static int userBalance;
    private static int totalBananas;

    private static final int outputTaskDelay = 1000; // milliseconds to queue up spins until we execute them at once
    private static boolean isOutputTaskScheduled = false;
    private static Timer outputTimer = new Timer();
    private static Map<MessageChannelUnion, Queue<MessageReceivedEvent>> eventQueues = new HashMap<>(); // map [channel] -> [queue of events since the last output]

    public static void scheduleOutputTask() {
        outputTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isOutputTaskScheduled = false;

                // go through the event queue per channel
                for(Map.Entry<MessageChannelUnion, Queue<MessageReceivedEvent>> entry: eventQueues.entrySet()) {
                    MessageChannelUnion channel = entry.getKey();
                    Queue<MessageReceivedEvent> queue = entry.getValue();

                    // execute all the spins for this channel, and send out the final combined output
                    StringBuilder outputBatchMessage = new StringBuilder();
                    while(queue.peek() != null) {
                        MessageReceivedEvent event = queue.poll();
                        performSpin(event, outputBatchMessage);
                        outputBatchMessage.append("...\n");
                    }
                    channel.sendMessage(outputBatchMessage.toString()).queue();
                }
            }
        }, outputTaskDelay);
    }

    public static void run(MessageReceivedEvent event) {
        // Timer instance only has one thread, so by scheduling this code with 0 delay we avoid concurrency issues with isOutputTaskScheduled and eventQueue
        outputTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                MessageChannelUnion channel = event.getChannel();
                if(!eventQueues.containsKey(channel)) {
                    eventQueues.put(channel, new LinkedList<>());
                }
                eventQueues.get(channel).add(event);

                if(!isOutputTaskScheduled) {
                    isOutputTaskScheduled = true;
                    scheduleOutputTask();
                }
            }
        }, 0);
    }

    private static void performSpin(MessageReceivedEvent event, StringBuilder outputMessage) {
        try {
            User author = event.getMessage().getAuthor();
            String userId = author.getId();
            String guildId = event.getGuild().getId();

            DBTools.openConnection();
            ResultSet authorSet = DBTools.selectGUILD_USER(guildId, userId);
             userBalance = authorSet.getInt("BANANA_CURRENT");
             totalBananas = authorSet.getInt("BANANA_TOTAL");

            if (!hasEnoughBananas(userBalance)) {
                sendMessageNotEnoughBananas(event, author, outputMessage);
                return;
            }

            userBalance -= BANANA_COST;
            processSpin(event, author, outputMessage);

            DBTools.updateGUILD_USER(guildId, userId, totalBananas, userBalance, null, null,null);
            DBTools.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean hasEnoughBananas(int userBalance) {
        return userBalance >= BANANA_COST;
    }

    private static void sendMessageNotEnoughBananas(MessageReceivedEvent event, User author, StringBuilder outputMessage) {
        outputMessage.append(author.getAsMention()).append(" You don't have enough bananas to spin!").append("\n");
    }

    private static void processSpin(MessageReceivedEvent event, User author, StringBuilder outputMessage) {
        int dropChance = random.nextInt(3); // 0, 1, or 2

        if (dropChance != 0) {
            handleDropChance(event, author, outputMessage);
        } else {
            int winnings = calculateWinnings(event, author, outputMessage);

            userBalance += winnings;
            totalBananas += winnings;

            generateWinningMessage(event, author, winnings, outputMessage);
        }
    }

    private static void handleDropChance(MessageReceivedEvent event, User author, StringBuilder outputMessage) {
        final String[] MEAN_MESSAGES = {
                "You are so stupid!", "Thats what you get for gambling, idiot.",
                "Get gunked!", "Hahahahaha", "You should give up", "Stop gambling!"
        };

        String randomMeanMessage = MEAN_MESSAGES[random.nextInt(MEAN_MESSAGES.length)];
        outputMessage.append(author.getAsMention()).append(" Uh-oh! You dropped 5 bananas on the way to the slot machine! 🍌🍌🍌🍌🍌 ").append(randomMeanMessage).append("\n");

        int currentJackpot = DBTools.selectJACKPOT(event.getGuild().getId());
        currentJackpot += 5;
        DBTools.updateJACKPOT(currentJackpot);
        outputMessage.append(":rotating_light:Banana Jackpot has reached: ").append(currentJackpot).append(" bananas! :rotating_light:").append("\n");
    }

    private static int calculateWinnings(MessageReceivedEvent event, User author, StringBuilder outputMessage) {
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
            winnings = handleJackpot(event, author, outputMessage);
        }
        return winnings;
    }

    private static int handleJackpot(MessageReceivedEvent event, User author, StringBuilder outputMessage) {
        int jackpot = DBTools.selectJACKPOT(event.getGuild().getId());
        outputMessage.append(author.getAsMention()).append(" 🎉🎉🎉 Jackpot! You spun and won ").append(jackpot).append(" bananas! 🎉🎉🎉 Holy moly!").append("\n");
        DBTools.updateJACKPOT(25);
        outputMessage.append("The banana Jackpot has reset to 25 bananas! Good luck!").append("\n");
        return jackpot;
    }

    private static void generateWinningMessage(MessageReceivedEvent event, User author, int winnings, StringBuilder outputString) {
        String winningMessage = switch (winnings) {
            case 5 -> " You spun and won 5 bananas! (You're an idiot!)";
            case 10 -> " You spun and won 10 bananas! Good job I guess?";
            case 15 -> " You spun and won 15 bananas! Thats cool";
            case 20 -> " You spun and won 20 bananas! Nice.";
            default -> " You spun and won " + winnings + " bananas!";
        };

        outputString.append(author.getAsMention()).append(winningMessage).append("\n");
    }
}
