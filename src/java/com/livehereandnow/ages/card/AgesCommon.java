/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.livehereandnow.ages.card;

/**
 *
 * @author mark
 */
public interface AgesCommon {

    String[] REASON_WHY = {
        "Game is not started yet!",
        "First round is only allow to take, not to build"

    };
    int REASON_GAME_NOT_YET_STARTED = 0;
    int REASON_FIRST_ROUND_NOT_ALLOW_TO_BUILD = 1;
//    int
//    int NO_ACTION_REASON_GAME_NOT_YET_STARTED=1;

//    從卡牌列指定位置拿牌所需要的內政點數
    int[] 從卡牌列指定位置拿牌所需要的內政點數 = {1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3};
//  文明結束回合時，依照剩餘人力庫數量，支付對應的食物。 0→6 1~4→4 5~8→3 9~12→2 12~16→1 17+→0
    int[] array回合維持支付食物對照表 = {6, 4, 4, 4, 4, 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 0};
//  0→6 1~4→4 5~8→2 9 0
    int[] array回合腐敗支付資源對照表 = {6, 4, 4, 4, 4, 2, 2, 2, 2, 0};

//    String[] 階段={"unknown","xxx","yyy"};
    int STYLE_政府區 = 100;
    int STYLE_領袖區 = 101;
    int STYLE_實驗室 = 109;
    int STYLE_建造中的奇蹟區 = 102;

    int STYLE_已完成的奇蹟 = 103;
    int STYLE_行動牌 = 105;
    int STYLE_普通 = 999;
    int STYLE_普通_藍點 = 1001;
    int STYLE_普通_黃點 = 1002;

//    String[] AGE_NAME = {"\uFF21", "\uFF29", "\uFF29\uFF29", "\uFF29\uFF29\uFF29"};//http://www.unicode.org/charts/PDF/UFF00.pdf
    String[] AGE_NAME = {"\uFF21", "I ", "II ", "III "};//http://www.unicode.org/charts/PDF/UFF00.pdf
    final String[] STAGE_NAME = {" ", "政治", "內政"};
//    final String[] AGE_NAME = {"A", "I", "II", "III"};
    final String FULLWIDTH_COLON = "\uFF1A";//;
    final String FULLWIDTH_SPACE = "\u3000";
    final String FULLWIDTH_LT_SIGN = "\uFF1C";// <
    final String FULLWIDTH_EQ_SIGN = "\uFF1D";// =
    final String FULLWIDTH_GT_SIGN = "\uFF1E";// >

}
