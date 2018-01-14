package io.coinpeeker.bot_hotssan.utils;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class AuthUtils {

    private static List<Long> userList = Arrays.asList(
            226524024L // 건표
            , 166243777L // 준성
            , 368794052L // 훈빈
            , 341972666L // 본재
            , 345295244L // 현민
            , 395791342L // 호준
            , -286833798L // 쇼미더머니로컬
    );


    public static boolean isAuthenticated(Long userId) {
        return userList.contains(userId);
    }
}
