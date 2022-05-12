import de.kosmos_lab.platform.plugins.web.chat.websocket.ChatWebSocketService;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ChatTests {

    @Test
    public static void testNick() {
        Assert.assertTrue(ChatWebSocketService.isValidNick("Jan"));
        Assert.assertTrue(ChatWebSocketService.isValidNick("jan"));
        Assert.assertFalse(ChatWebSocketService.isValidNick("js"),"nicks should be > 2 characters");
        Assert.assertFalse(ChatWebSocketService.isValidNick("j"),"nicks should be > 2 characters");
        Assert.assertTrue(ChatWebSocketService.isValidNick("afsfasnkjnsfjnas"),"max lenght nick failed");
        Assert.assertFalse(ChatWebSocketService.isValidNick("1234567890123456"),"nicks cannot start with a number");
        Assert.assertTrue(ChatWebSocketService.isValidNick("_123456789012345"),"nicks starting with _ should be fine");
        Assert.assertTrue(ChatWebSocketService.isValidNick("Test[Tset]"));
        Assert.assertFalse(ChatWebSocketService.isValidNick("0Test[Tset]"),"nicks cannot start with a number");
        Assert.assertFalse(ChatWebSocketService.isValidNick("asfansfjasasdasdasdnfkjnasfjnasf"));
        Assert.assertFalse(ChatWebSocketService.isValidNick("ääööaspdas"),"nicks cannot contain special chars");
        Assert.assertFalse(ChatWebSocketService.isValidNick("aääööaspdas"),"nicks cannot contain special chars");
        Assert.assertFalse(ChatWebSocketService.isValidNick("asfansfjasasdasdasdnfkjnasfjnasf"),"nick should be <=16 characters");
    }
}
