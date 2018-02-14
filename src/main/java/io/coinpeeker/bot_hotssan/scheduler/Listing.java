package io.coinpeeker.bot_hotssan.scheduler;

import java.io.IOException;

public interface Listing {

    void init() throws IOException;

    void inspectListedCoin() throws IOException;

}
