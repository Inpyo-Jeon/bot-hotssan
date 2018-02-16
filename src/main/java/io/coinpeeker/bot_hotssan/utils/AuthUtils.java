package io.coinpeeker.bot_hotssan.utils;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthUtils {

    private List<Long> userList = Arrays.asList(
            226524024L // 건표
            , 166243777L // 준성
            , 368794052L // 훈빈
            , 341972666L // 본재
            , 345295244L // 현민
            , 395791342L // 호준
            , 458814507L // 인표
            , -286833798L // 쇼미더머니로컬
            , -259666461L // 쇼미더머니로컬(인표)
            , -294606763L // 쇼미더데브
            , -300048567L // 쇼미더리얼
            , -277619118L // 인표친구들그룹
    );

    public boolean isAuthenticated(Long userId) {
        return userList.contains(userId);
    }
}
