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

        Pattern pattern = Pattern.compile("(^/)([a-z]|[A-Z])*$");
        Matcher matcher = pattern.matcher(instruction);

        if(matcher.find()){
            result.append(analyzeCommand(instruction));
        } else {
            result.append("명령어 구성이 잘못되었습니다.");
        }

        return result.toString();
    }





    // 입력된 명령어 분석하는 메서드
    // TODO : 클래스 내의 정규식들 다시 한 번 확인
    public String analyzeCommand(String instruction){

        Pattern p                   = Pattern.compile("^/");
        Matcher matcher             = p.matcher(instruction);
        String  replaceInstruction  = matcher.replaceAll("").toUpperCase();
        String  result              = "";


        try{
            GeneralInstruction.valueOf(replaceInstruction);
            // 공통 명령어
            // ex) result = 공통명령어 클래스 결과값
        } catch(IllegalArgumentException e){
            try{
                result = coinContainer.execute(CoinInstruction.valueOf(replaceInstruction).toString());
            } catch (IllegalArgumentException errorFindInstruction){
                result = "정의되어있지 않은 명령어입니다.";
            }
        }
        return result;
    }






    enum GeneralInstruction{
        PP, DD
    }

    enum CoinInstruction{
        DENT, BTC
    }
}