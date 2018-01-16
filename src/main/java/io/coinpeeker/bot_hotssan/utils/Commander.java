package io.coinpeeker.bot_hotssan.utils;

import io.coinpeeker.bot_hotssan.external.CoinContainer;
import io.coinpeeker.bot_hotssan.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Commander {

    @Autowired
    private CoinContainer coinContainer;

    public String execute(String instruction) {
        StringBuilder result = new StringBuilder();

        result.append("입력한 명령어 : ");
        result.append(instruction);

        // TODO : 관장하는 클래스 구성
        // btc(비트코인) 일 때만 시세확인
        if("/btc".equals(instruction)){
            Pattern p = Pattern.compile("[^a-z]");
            Matcher matcher = p.matcher(instruction);
            String replaceInstruction = matcher.replaceAll("");

            result.append(coinContainer.execute(replaceInstruction.toUpperCase()));
        }

        return result.toString();
    }
}
