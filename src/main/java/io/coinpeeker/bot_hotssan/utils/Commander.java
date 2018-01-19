package io.coinpeeker.bot_hotssan.utils;

import io.coinpeeker.bot_hotssan.external.CoinContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

//        Pattern pattern = Pattern.compile("(^/)([ㄱ-ㅎ|ㅏ-ㅣ|가-힣])*$");
//        Matcher matcher = pattern.matcher(instruction);
//        if(matcher.find()){
//            result.append(analyzeCommand(instruction));
//        } else {
//            result.append("명령어 구성이 잘못되었습니다.");
//
//        }
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


//    // 입력된 명령어 분석하는 메서드
//    // TODO : 클래스 내의 정규식들 다시 한 번 확인
//    public String analyzeCommand(String instruction){
//
//        Pattern p                   = Pattern.compile("^/");
//        Matcher matcher             = p.matcher(instruction);
//        String  replaceInstruction  = matcher.replaceAll("").toUpperCase();
//        String  result              = "";
//
//        try{
//            GeneralInstruction.valueOf(replaceInstruction);
//            // 공통 명령어
//            // ex) result = 공통명령어 클래스 결과값
//        } catch(IllegalArgumentException e){
//            try{
//                result = coinContainer.execute(CoinInstruction.valueOf(replaceInstruction.toString()).getName());
//            } catch (IllegalArgumentException errorFindInstruction){
//                result = "정의되어있지 않은 명령어입니다.";
//            }
//        }
//        return result;
//    }





    enum GeneralInstruction{
        PP, DD
    }

    enum CoinInstruction{
        BTC, BCC, ETH, DASH, ZEC, XMR, BTG, LTC, NEO, REP, QTUM, ETC, LSK, OMG, STRAT, PIVX, WAVES, KMD, ARK, VTC, STEEM, SBD, MTL, XRP, STORJ, ARDR, POWR, TIX, XEM, GRS, ADA, EMC2, XLM, MER, SNT;
    }


//    enum CoinInstruction{
//        비트코인("BTC"),
//        비트코인캐시("BCC"),
//        이더리움("ETH"),
//        대시("DASH"),
//        지캐시("ZEC"),
//        모네로("XMR"),
//        비트코인골드("BTG"),
//        라이트코인("LTC"),
//        네오("NEO"),
//        어거("REP"),
//        퀀텀("QTUM"),
//        이더리움클래식("ETC"),
//        리스크("LSK"),
//        오미세고("OMG"),
//        스트라티스("STRAT"),
//        피벡스("PIVX"),
//        웨이브("WAVES"),
//        코모도("KMD"),
//        아크("ARK"),
//        버트코인("VTC"),
//        스팀("STEEM"),
//        스팀달러("SBD"),
//        메탈("MTL"),
//        리플("XRP"),
//        스토리지("STORJ"),
//        아더("ARDR"),
//        파워렛저("POWR"),
//        블록틱스("TIX"),
//        뉴이코노미무브먼트("XEM"),
//        그로스톨코인("GRS"),
//        에이다("ADA"),
//        아인스타이늄("EMC2"),
//        스텔라루멘("XLM"),
//        머큐리("MER"),
//        스테이터스네트워크토큰("SNT");
//
//        private final String name;
//
//        public String getName(){
//            return name;
//        }
//
//        private CoinInstruction(String name){
//            this.name = name;
//        }
//    }
}