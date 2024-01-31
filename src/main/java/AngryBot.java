import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildTimeoutEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateAfkTimeoutEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import javax.security.auth.login.LoginException;

public class AngryBot extends ListenerAdapter {

    static MessageReceivedEvent mostRecentEvent;
    static EmbedBuilder eb = new EmbedBuilder();
    static final String command = "<";
    static Map<String, Runnable> commands = new HashMap<>();
    private static JDA jda;
    private  User brendan;
    private  PrivateChannel brendansDM ;
    public AngryBot() {
        Tools.initializeParser();
    }


    public static void main(String[] args) throws LoginException, SQLException {
        commands.put("sherpa", () -> Sherpa.run(mostRecentEvent));
        commands.put("scramble", () -> Scramble.run(mostRecentEvent));
        commands.put("embee", () -> Embee.run(mostRecentEvent));
        commands.put("crazydazed", () -> CrazyDazed.run(mostRecentEvent));
        commands.put("gunk", () -> Gunk.run(mostRecentEvent));
        commands.put("ungunk", () -> UnGunk.run(mostRecentEvent));
        commands.put("score", () -> BananaScore.run(mostRecentEvent));

        Sherpa.initializeList();

        jda = JDABuilder.createDefault(Config.BOT_TOKEN)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new AngryBot())
                .setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
                .setMemberCachePolicy(MemberCachePolicy.ALL) // ignored if chunking enabled
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .build();



    }


    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        try {
            DBTools.openConnection();
            List<Member> members = event.getGuild().getMembers();
            String GID = event.getGuild().getId();
            for (Member m : members) {
                DBTools.insertGUILD_USER(GID, m.getId());
            }
            DBTools.closeConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        try {
            DBTools.openConnection();
            DBTools.insertGUILD_USER(event.getGuild().getId(), event.getMember().getId());
            DBTools.closeConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void onGuildMemberUpdateTimeOut(GuildMemberUpdateTimeOutEvent event) {
        brendan = jda.getUserById("708499135770394634");
        brendansDM = brendan.openPrivateChannel().complete();
        OffsetDateTime timeoutEnd = event.getNewTimeOutEnd();
        if (timeoutEnd != null) {
            OffsetDateTime now = OffsetDateTime.now();
            long remainingSeconds = Duration.between(now, timeoutEnd).getSeconds();
            String formattedDuration = Tools.formatDuration(remainingSeconds);
            brendansDM.sendMessage(event.getUser().getEffectiveName() + " has been timed out for " + formattedDuration).queue();
        }else {
            System.out.println("Timeout end time is null for user " + event.getUser().getEffectiveName());
        }
        if (event.getUser().getId().equals("328689134606614528")||event.getUser().getId().equals("976597595831033916")){
            System.out.println("Timeout User: " + event.getUser().getId()+ "  " + event.getNewTimeOutEnd());
            event.getMember().removeTimeout().queue();
        }
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;


        int randomNum = ThreadLocalRandom.current().nextInt(0, 1001);
        System.out.println("randomNum = " + randomNum);
        if(randomNum < 50){
            event.getMessage().addReaction(Emoji.fromUnicode("U+1F34C")).queue();
            try {
                DBTools.openConnection();
                ResultSet set = DBTools.selectGUILD_USER(event.getGuild().getId(),event.getAuthor().getId());
                int total = 1+ set.getInt("BANANA_TOTAL");
                int current = 1+ set.getInt("BANANA_CURRENT");
                DBTools.updateGUILD_USER(event.getGuild().getId(),event.getAuthor().getId(),total,current,null,null);
                DBTools.closeConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if(randomNum == 51 ){
            event.getMessage().addReaction(Emoji.fromUnicode("U+1F1E8")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("U+1F1F1")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("U+1F1EA")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("U+1F1FC")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("U+1F1FF")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("U+1F1F7")).queue();
        }

        mostRecentEvent = event;


        ListIterator<Role> roles = Objects.requireNonNull(event.getMember()).getRoles().listIterator();
        Message message = event.getMessage();
        String content = message.getContentRaw().toLowerCase();

 if(event.getAuthor().getId().equals("1149411432778174474")){
            if (content.equalsIgnoreCase("No")) {

                try {
                    event.getGuildChannel().sendMessage("yea").queue();
                } catch (NullPointerException E) {
                    E.printStackTrace();
                }
                return;
            }
            if (content.equalsIgnoreCase("No u")) {

                try {
                    event.getGuildChannel().sendMessage("No u").queue();
                } catch (NullPointerException E) {
                    E.printStackTrace();
                }
                return;
            }
        }       else if(event.getAuthor().getId().equals("764537809045815322")){
            if (content.equalsIgnoreCase("boot fucker")) {

                try {
                    event.getGuildChannel().sendMessage("No u").queue();
                } catch (NullPointerException E) {
                    E.printStackTrace();
                }
                return;
            }
            if (content.equalsIgnoreCase("No u")) {

                try {
                    event.getGuildChannel().sendMessage("No u").queue();
                } catch (NullPointerException E) {
                    E.printStackTrace();
                }
                return;
            }
        }

        if (content.startsWith(command)) {
                    String commandContent = content.substring(1).split(" ")[0].toLowerCase();
                    try {
                        commands.get(commandContent).run();
                    } catch (NullPointerException E) {
                        E.printStackTrace();
                    }
               return;
                }


    ListIterator<String> triggers = Sherpa.TriggerWords.listIterator();
        while (triggers.hasNext()) {
            if (content.contains(triggers.next()) && Tools.containsParserTypes(content)){
                Sherpa.replySherpa(event);
                return;
            }
        }

    }
}
