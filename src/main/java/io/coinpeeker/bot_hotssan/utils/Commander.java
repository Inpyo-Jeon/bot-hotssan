package io.coinpeeker.bot_hotssan.utils;

import org.springframework.stereotype.Component;

@Component
public class Commander {

    public String execute(String instruction) {
        StringBuilder result = new StringBuilder();

        result.append("입력한 명령어 : ");
        result.append(instruction);

        return result.toString();
    }
}
