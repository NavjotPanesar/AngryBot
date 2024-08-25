import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.InputStream;

public class Card {
    public static void addCard(MessageReceivedEvent event) {
        try {
            DBTools.openConnection();

            var params = event.getMessage().getContentDisplay().substring(1).split("<");
            int rowCount = DBTools.insertCard(params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8], 0, params[9], true);
            event.getMessage().reply(rowCount > 0 ? "nice. latest card id is " + DBTools.getLatestCardId() : "nope").queue();

            DBTools.closeConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void viewCard(MessageReceivedEvent event) {
        try {
            DBTools.openConnection();

            var messageText = event.getMessage().getContentDisplay();
            var params = event.getMessage().getContentDisplay().split(" ");
            var card_id = Integer.parseInt(params[1]);
            var isGif = params.length >= 3 && (params[2]).equals("gif");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Config.CARD_URL())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            CardService service = retrofit.create(CardService.class);

            var res = isGif ? service.getSummon(card_id).execute() : service.getCard(card_id).execute();
            ResponseBody body = res.body();
            InputStream imageStream = body.byteStream();
            event.getMessage().reply("Ebic card !!!").addFiles(FileUpload.fromData(imageStream, isGif ? "card.gif" : "card.png")).queue();

            DBTools.closeConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void addImage(MessageReceivedEvent event) {
        try {
            DBTools.openConnection();

            var messageText = event.getMessage().getContentDisplay();
            var params = event.getMessage().getContentDisplay().split(" ");
            var image_name = params[1];

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Config.CARD_URL())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            CardService service = retrofit.create(CardService.class);

            var attachment = event.getMessage().getAttachments().get(0);
            var stream = attachment.getProxy().download().get();

            var part = MultipartBody.Part.createFormData(
                    "image",
                    "myPic",
                    RequestBody.create(stream.readAllBytes(), MediaType.parse("image/*"))
            );
            var res = service.uploadImage(RequestBody.create(image_name, MediaType.parse("text/plain")), part).execute();

            event.getMessage().reply(res.body() == null ? "nope": res.body()).queue();

            DBTools.closeConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
