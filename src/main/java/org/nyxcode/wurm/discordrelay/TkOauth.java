package org.nyxcode.wurm.discordrelay;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.misc.Href;
import org.takes.rq.RqHref;

import java.io.IOException;

public class TkOauth implements Take {
    public Response act(Request req) throws IOException {
        Href href = new RqHref.Base(req).href();
        Iterable<String> token = href.param("token");

        return null;
    }
}
