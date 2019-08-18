package utils;

import play.i18n.Lang;
import play.i18n.Messages;
import play.i18n.MessagesApi;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public class MessageApi {
    private final play.i18n.MessagesApi messagesApi;

    @Inject
    MessageApi(MessagesApi messagesApi) {
        this.messagesApi = messagesApi;
    }

    public String getMessage(String key, Object... args) {
        Collection<Lang> candidates = Collections.singletonList(new Lang(Locale.KOREA));
        Messages messages = messagesApi.preferred(candidates);
        return messages.at(key, args);
    }
}
