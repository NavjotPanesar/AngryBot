import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Jackpot {

    public static void run(MessageReceivedEvent event) {
        try {
            DBTools.openConnection();
            int currentJackpot = DBTools.selectJACKPOT(event.getGuild().getId());

            event.getChannel().sendMessage(":tada: Exciting News! The current Banana Jackpot is: " + currentJackpot + " bananas! :tada:").queue();
            DBTools.closeConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
